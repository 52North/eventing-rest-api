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

import org.n52.eventing.rest.parameters.ParameterInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodDefinition;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodInstance;
import org.n52.eventing.rest.deliverymethods.UnknownDeliveryMethodException;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.eventing.rest.templates.TemplateInstance;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.rest.users.UnknownUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.n52.eventing.rest.users.UsersService;
import org.n52.eventing.rest.publications.PublicationsService;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsService;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummySubscriptionsDao implements SubscriptionsService, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DummySubscriptionsDao.class);

    private final Map<String, SubscriptionInstance> subscriptions = new HashMap<>();

    @Autowired
    private PublicationsService publicationsDao;

    @Autowired
    private UsersService usersDao;

    @Autowired
    private DeliveryMethodsService deliveryMethodsDao;

    @Autowired
    private TemplatesDao templatesDao;

    @Override
    public synchronized boolean hasSubscription(String id) {
        return subscriptions.containsKey(id);
    }

    @Override
    public synchronized List<SubscriptionInstance> getSubscriptions(Pagination p) {
        return Collections.unmodifiableList(new ArrayList<>(subscriptions.values()));
    }

    @Override
    public synchronized SubscriptionInstance getSubscription(String id) throws UnknownSubscriptionException {
        if (!hasSubscription(id)) {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }

        return subscriptions.get(id);
    }

    @Override
    public synchronized void addSubscription(String subId, SubscriptionInstance subscription) {
        this.subscriptions.put(subId, subscription);
    }

    @Override
    public synchronized SubscriptionInstance updateEndOfLife(String id, DateTime eol) throws UnknownSubscriptionException {
        if (hasSubscription(id)) {
            SubscriptionInstance sub = this.subscriptions.get(id);
            sub.setModified(new DateTime());
            sub.setEndOfLife(eol);
            return sub;
        }
        else {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }
    }

    @Override
    public synchronized SubscriptionInstance updateStatus(String id, boolean enabled) throws UnknownSubscriptionException {
        if (hasSubscription(id)) {
            SubscriptionInstance sub = this.subscriptions.get(id);
            sub.setModified(new DateTime());
            sub.setEnabled(enabled);
            return sub;
        }
        else {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }
    }

    @Override
    public synchronized void remove(String id) throws UnknownSubscriptionException {
        if (hasSubscription(id)) {
            this.subscriptions.remove(id);
        }
        else {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("initializing subscriptions...");

        try {
            SubscriptionInstance sub = new SubscriptionInstance("dummy-sub", "dummy-sub yeah", "this subscription is set up!");
            sub.setUser(this.usersDao.getUser("dummy-user"));
            sub.setPublicationId(this.publicationsDao.getPublication("dummy-pub", null).getId());
            sub.addDeliveryMethod(createDeliveryInstance(this.deliveryMethodsDao.getDeliveryMethod("email"), "peterchen@paulchen.de"));
            sub.setEndOfLife(new DateTime().plusMonths(2));
            sub.setEnabled(true);

            List<ParameterInstance> params = new ArrayList<>();
            params.add(new ParameterInstance("observedProperty", "Wasserstand", "text"));
            params.add(new ParameterInstance("sensorID", "Wasserstand_Opladen", "text"));
            params.add(new ParameterInstance("thresholdValue", 55.2, "number"));
            sub.setTemplate(createTemplateInstance(this.templatesDao.getTemplate("overshootUndershoot"), params));

            subscriptions.put("dummy-sub", sub);
        } catch (UnknownPublicationsException | UnknownUserException
                | UnknownTemplateException | UnknownDeliveryMethodException ex) {
            LOG.warn(ex.getMessage(), ex);
        }

    }

    private DeliveryMethodInstance createDeliveryInstance(DeliveryMethodDefinition deliveryMethod, String to) {
        DeliveryMethodInstance instance = new DeliveryMethodInstance(deliveryMethod.getId(),
                Collections.singletonMap("to", new ParameterInstance("to", to, "text")));
        return instance;
    }

    private TemplateInstance createTemplateInstance(TemplateDefinition template, List<ParameterInstance> params) {
        TemplateInstance instance = new TemplateInstance(template.getId(),
                params.stream().collect(Collectors.toMap(ParameterInstance::getName, Function.identity())));
        return instance;
    }

}
