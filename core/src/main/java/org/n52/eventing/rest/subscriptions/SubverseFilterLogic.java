/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package org.n52.eventing.rest.subscriptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodInstance;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsService;
import org.n52.eventing.rest.eventlog.EventLogEndpoint;
import org.n52.eventing.rest.eventlog.EventLogStore;
import org.n52.eventing.rest.parameters.ParameterInstance;
import org.n52.eventing.rest.templates.FilterInstanceGenerator;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.engine.FilterEngine;
import org.n52.subverse.engine.SubscriptionRegistrationException;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.subscription.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubverseFilterLogic implements FilterLogic {

    private static final Logger LOG = LoggerFactory.getLogger(SubverseFilterLogic.class);


    @Autowired
    private EventLogStore eventLogStore;

    @Autowired
    private DeliveryMethodsService deliveryMethodsDao;

    @Autowired
    private FilterEngine engine;

    private final FilterInstanceGenerator filterInstanceGenerator = new FilterInstanceGenerator();
    private final Map<String, Subscription> subscriptionToRuleMap = new HashMap<>();

    @Override
    public void internalSubscribe(SubscriptionInstance subscription, TemplateDefinition template) throws InvalidSubscriptionException {
        /*
        * resolve delivery endpoint
        */
        List<DeliveryEndpoint> endpoints = subscription.getDeliveryMethods().stream().map((DeliveryMethodInstance dm) -> {
            try {
                return this.deliveryMethodsDao.createDeliveryEndpoint(dm,
                        subscription.getPublicationId());
            } catch (InvalidSubscriptionException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        endpoints.add(new EventLogEndpoint(20, subscription, eventLogStore, String.format("Rule match for Template '%s' with Parameters: %s",
                template.getId(), subscription.getTemplate().getParameters())));
        BrokeringDeliveryEndpoint brokeringEndpoint = new BrokeringDeliveryEndpoint(endpoints);

        /*
        * register at engine
        */
        Map<String, ParameterInstance> params = subscription.getTemplate().getParameters();
        params.forEach((String t, ParameterInstance u) -> {
            u.setName(t);
        });
        String filterInstance = this.filterInstanceGenerator.generateFilterInstance(
                template, params.values());
        try {
            Subscription subverseSub = wrapToSubverseSubscription(subscription,
                    filterInstance, subscription.getPublicationId());
            this.engine.register(subverseSub, brokeringEndpoint);

            /*
            * remember subverseSub for later removal
            */
            synchronized (this) {
                this.subscriptionToRuleMap.put(subscription.getId(), subverseSub);
            }
        } catch (SubscriptionRegistrationException ex) {
            LOG.warn("Could not register subscription at engine");
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }

    }

    private Subscription wrapToSubverseSubscription(SubscriptionInstance subscription,
            String filterInstance, String pubId) throws InvalidSubscriptionException {
        try {
            XmlObject filterXml = XmlObject.Factory.parse(filterInstance);
            Subscription result = new Subscription(
                    subscription.getId(), new SubscribeOptions(pubId,
                            null,
                            filterXml,
                            null,
                            null,
                            Collections.emptyMap(),
                            null), null);
            return result;
        } catch (XmlException ex) {
            throw new InvalidSubscriptionException("Currently only valid XML filter definitions allowed", ex);
        }
    }

    @Override
    public void remove(String id) {
        Subscription sub;
        synchronized (this) {
            sub = this.subscriptionToRuleMap.get(id);
        }

        try {
            this.engine.removeSubscription(sub.getId());
        } catch (org.n52.subverse.subscription.UnknownSubscriptionException ex) {
            LOG.warn("Could not remove subscription", ex);
        }
    }
}