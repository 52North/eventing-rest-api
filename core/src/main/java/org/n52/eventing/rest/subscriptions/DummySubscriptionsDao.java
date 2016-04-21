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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.eventing.rest.Constructable;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsDao;
import org.n52.eventing.rest.deliverymethods.UnknownDeliveryMethodException;
import org.n52.eventing.rest.publications.PublicationsDao;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.rest.users.UnknownUserException;
import org.n52.eventing.rest.users.UsersDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummySubscriptionsDao implements SubscriptionsDao, Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(DummySubscriptionsDao.class);
    public static final DateTimeFormatter ISO_FORMATTER = ISODateTimeFormat.dateTime();

    private final Map<String, Subscription> subscriptions = new HashMap<>();

    @Autowired
    private PublicationsDao publicationsDao;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private DeliveryMethodsDao deliveryMethodsDao;

    @Autowired
    private TemplatesDao templatesDao;

    @Override
    public synchronized boolean hasSubscription(String id) {
        return subscriptions.containsKey(id);
    }

    @Override
    public synchronized List<Subscription> getSubscriptions() {
        return Collections.unmodifiableList(new ArrayList<>(subscriptions.values()));
    }

    @Override
    public synchronized Subscription getSubscription(String id) throws UnknownSubscriptionException {
        if (!hasSubscription(id)) {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }

        return subscriptions.get(id);
    }

    @Override
    public synchronized void addSubscription(String subId, Subscription subscription) {
        this.subscriptions.put(subId, subscription);
    }

    @Override
    public synchronized void updateEndOfLife(String id, DateTime eol) throws UnknownSubscriptionException {
        if (hasSubscription(id)) {
            this.subscriptions.get(id).setEndOfLife(eol.toString(ISO_FORMATTER));
        }
        else {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }
    }

    @Override
    public synchronized void updateStatus(String id, Subscription.Status status) throws UnknownSubscriptionException {
        if (hasSubscription(id)) {
            this.subscriptions.get(id).setStatus(status);
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
    public void construct() {
        LOG.info("initializing subscriptions...");

        try {
            Subscription sub = new Subscription("dummy-sub", "dummy-sub yeah", "this subscription is set up!");
            sub.setUser(this.usersDao.getUser("dummy-user"));
            sub.setPublicationId(this.publicationsDao.getPublication("dummy-pub").getId());
            sub.setDeliveryMethodId(this.deliveryMethodsDao.getDeliveryMethod("email").getId());
            sub.setEndOfLife(new DateTime().plusMonths(2).toString(ISO_FORMATTER));
            sub.setStatus(Subscription.Status.ENABLED);
            sub.setTemplateId(this.templatesDao.getTemplate("overshootUndershoot").getId());
            sub.setConsumer("peterchen@paulchen.de");
            subscriptions.put("dummy-sub", sub);
        } catch (UnknownPublicationsException | UnknownUserException
                | UnknownTemplateException | UnknownDeliveryMethodException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

}
