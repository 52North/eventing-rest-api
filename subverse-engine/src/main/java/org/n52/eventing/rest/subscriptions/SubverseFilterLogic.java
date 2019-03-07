/*
 * Copyright (C) 2016-2019 52°North Initiative for Geospatial Open Source
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

import org.n52.eventing.rest.model.impl.SubscriptionImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodInstance;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsService;
import org.n52.eventing.rest.eventlog.EventLogEndpoint;
import org.n52.eventing.rest.eventlog.EventLogStore;
import org.n52.eventing.rest.parameters.ParameterInstance;
import org.n52.eventing.rest.templates.FilterInstanceGenerator;
import org.n52.eventing.rest.model.TemplateDefinition;
import org.n52.eventing.rest.model.impl.TemplateDefinitionImpl;
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
    public String internalSubscribe(org.n52.eventing.rest.model.Subscription sub, TemplateDefinition template) throws InvalidSubscriptionException {
        if (!(sub instanceof SubscriptionImpl)) {
            throw new InvalidSubscriptionException("Unsupported subscription type: "+ sub);
        }

        SubscriptionImpl subscription = (SubscriptionImpl) sub;

        /*
        * resolve delivery endpoint
        */
        List<DeliveryEndpoint> endpoints = subscription.getDeliveryMethods().stream().map((DeliveryMethodInstance dm) -> {
            try {
                DeliveryEndpoint result = this.deliveryMethodsDao.createDeliveryEndpoint(dm,
                        subscription.getPublicationId());
                String effLoc = result.getEffectiveLocation();
                if (effLoc != null) {
                    dm.setDetails(Collections.singletonMap("effectiveLocation", result.getEffectiveLocation()));
                }
                return result;
            } catch (InvalidSubscriptionException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        endpoints.add(new EventLogEndpoint(20, subscription, eventLogStore));
        BrokeringDeliveryEndpoint brokeringEndpoint = new BrokeringDeliveryEndpoint(endpoints);

        /*
        * register at engine
        */

        String filterInstance = null;
        Map<String, ParameterInstance> params = null;
        if (template != null && template instanceof TemplateDefinitionImpl) {
            params = subscription.getNotificationInstance().getParameters();
            params.forEach((String t, ParameterInstance u) -> {
                u.setName(t);
            });
            filterInstance = this.filterInstanceGenerator.generateFilterInstance(
                (TemplateDefinitionImpl) template, params.values());
        }

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

        return UUID.randomUUID().toString();
    }

    private Subscription wrapToSubverseSubscription(SubscriptionImpl subscription,
            String filterInstance, String pubId) throws InvalidSubscriptionException {
        try {
            XmlObject filterXml = null;
            if (filterInstance != null) {
                filterXml = XmlObject.Factory.parse(filterInstance);
            }

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
