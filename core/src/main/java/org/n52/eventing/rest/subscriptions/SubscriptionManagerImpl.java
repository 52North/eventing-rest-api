/*
 * Copyright (C) 2016-2017 52°North Initiative for Geospatial Open Source
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

import org.n52.eventing.rest.model.Subscription;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.n52.eventing.rest.factory.TemplatesDaoFactory;
import org.n52.eventing.rest.model.TemplateDefinition;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.rest.users.User;
import org.n52.subverse.termination.Terminatable;
import org.n52.subverse.termination.TerminationScheduler;
import org.n52.subverse.termination.UnknownTerminatableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.n52.eventing.rest.publications.PublicationsService;
import org.n52.subverse.termination.QuartzTerminationScheduler;
import org.springframework.beans.factory.DisposableBean;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionManagerImpl implements SubscriptionManager, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionManagerImpl.class);

    @Autowired
    private SubscriptionsService dao;

    @Autowired
    private PublicationsService publicationsDao;

    @Autowired
    private TemplatesDaoFactory templatesDaoFactory;

    @Autowired
    private FilterLogic filterLogic;

    private TerminationScheduler terminator;

    private final Map<Subscription, SubscriptionManagerImpl.SubscriptionTerminatable> subscriptionToTerminatableMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        terminator = new QuartzTerminationScheduler();

        LOG.info("Retrieveing persisted subscriptions...");
        AtomicInteger count = new AtomicInteger();
        this.dao.getSubscriptions(null).getResult().stream().forEach(s -> {
            LOG.info("Registering subscription {}", s.getId());
            try {
                if (s.getTemplateInstance() != null) {
                    filterLogic.internalSubscribe(s, templatesDaoFactory.newDao().getTemplate(s.getTemplateInstance().getId()));
                    count.getAndIncrement();
                }
            } catch (UnknownTemplateException ex) {
                LOG.warn("Could not find template for subscription", ex);
            } catch (InvalidSubscriptionException ex) {
                LOG.warn("Could not create subscription", ex);
            }
        });
        LOG.info("Registered {} persisted subscriptions...", count);
    }

    @Override
    public void destroy() throws Exception {
        this.terminator.shutdown();
    }



    @Override
    public String subscribe(Subscription subDef, User user) throws InvalidSubscriptionException {
        throwExceptionOnNullOrEmpty(subDef.getPublicationId(), "publicationId");

        String pubId = subDef.getPublicationId();
        if (!this.publicationsDao.hasPublication(pubId)) {
            throw new InvalidSubscriptionException("Publication unknown: "+pubId);
        }

        TemplateDefinition template = null;
        String desc;
        if (subDef.getTemplateInstance() != null) {
            try {
                template = this.templatesDaoFactory.newDao().getTemplate(subDef.getTemplateInstance().getId());
            } catch (UnknownTemplateException ex) {
                LOG.warn(ex.getMessage());
                LOG.debug(ex.getMessage(), ex);
                throw new InvalidSubscriptionException("Template unknown: "+pubId);
            }
            desc = String.format("Subscription using template %s. Parameters: %s", template.getId(), subDef.getTemplateInstance().getParameters());
        }
        else {
            desc = String.format("Subscription for publication: %s", pubId);
        }

        DateTime now = new DateTime();

        String label = Optional.ofNullable(subDef.getLabel()).orElse(desc);

        subDef.setLabel(label);
        subDef.setPublicationId(pubId);
        subDef.setUser(user);
        subDef.setCreated(now);
        subDef.setModified(now);

        //do the actual subscription part
        String subId = filterLogic.internalSubscribe(subDef, template);
        subDef.setId(subId);

        if (subDef.getEndOfLife() != null) {
            SubscriptionManagerImpl.SubscriptionTerminatable term = new SubscriptionManagerImpl.SubscriptionTerminatable(subDef);
            terminator.scheduleTermination(term);
            subscriptionToTerminatableMap.put(subDef, term);
        }

        /*
        * finally add to the DAO
        */
        this.dao.addSubscription(subId, subDef);

        return subId;
    }


    @Override
    public void updateSubscription(SubscriptionUpdate subDef, User user) throws InvalidSubscriptionException {
        String eolString = subDef.getEndOfLife();
        if (eolString != null && !eolString.isEmpty()) {
            DateTime eol = parseEndOfLife(eolString);
            try {
                Subscription subInstance = this.dao.updateEndOfLife(subDef.getId(), eol);
                changeEndOfLife(subInstance, eol);
            } catch (UnknownSubscriptionException ex) {
                throw new InvalidSubscriptionException(ex.getMessage(), ex);
            }

        }

        Boolean enabled = subDef.getEnabled();
        try {
            this.dao.updateStatus(subDef.getId(), enabled);
        } catch (UnknownSubscriptionException ex) {
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }

        if (enabled) {
            resume(subDef.getId());
        }
        else {
            pause(subDef.getId());
        }
    }

    private DateTime parseEndOfLife(String eolString) throws InvalidSubscriptionException {
        try {
            return new DateTime(eolString);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidSubscriptionException("Not a valid xs:date: "+eolString);
        }
    }

    @Override
    public void removeSubscription(String id, User user) throws InvalidSubscriptionException {
        if (this.dao.hasSubscription(id)) {
            remove(id);
        }
        else {
            throw new InvalidSubscriptionException("Unknown subscription: "+id);
        }
    }

    private void resume(String id) {
        LOG.debug("TODO: Implement resume");
    }

    private void pause(String id) {
        LOG.debug("TODO: Implement pause");
    }

    private void changeEndOfLife(Subscription subscription, DateTime eol) {
        if (subscriptionToTerminatableMap.containsKey(subscription)) {
            try {
                terminator.cancelTermination(subscriptionToTerminatableMap.get(subscription));
            } catch (UnknownTerminatableException ex) {
                LOG.warn("Could not cancel termination", ex);
            }
        }

        SubscriptionTerminatable term = new SubscriptionTerminatable(subscription);
        terminator.scheduleTermination(term);
        subscriptionToTerminatableMap.put(subscription, term);
    }

    private void remove(String id) throws InvalidSubscriptionException {
        this.filterLogic.remove(id);
        try {
            this.dao.remove(id);
        } catch (UnknownSubscriptionException ex) {
            throw new InvalidSubscriptionException("Subscription is not known: "+ id, ex);
        }
    }

    private void throwExceptionOnNullOrEmpty(String value, String key) throws InvalidSubscriptionException {
        if (value == null || value.isEmpty()) {
            throw new InvalidSubscriptionException(String.format("Parameter %s cannot be null or empty", key));
        }
    }


    public class SubscriptionTerminatable implements Terminatable {

        private final Subscription subscription;

        public SubscriptionTerminatable(Subscription subscription) {
            this.subscription = subscription;
        }

        public Subscription getSubscription() {
            return subscription;
        }

        @Override
        public void terminate() {
            try {
                removeSubscription(subscription.getId(), subscription.getUser());
            } catch (InvalidSubscriptionException ex) {
                LOG.warn("Could not terminate subscription", ex);
            }
        }

        @Override
        public DateTime getEndOfLife() {
            return subscription.getEndOfLife();
        }

    }

}
