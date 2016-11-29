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

package org.n52.eventing.wv.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodInstance;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.eventing.rest.subscriptions.SubscriptionsService;
import org.n52.eventing.rest.subscriptions.UnknownSubscriptionException;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.SubscriptionDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSubscriptionDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;
import org.n52.eventing.wv.security.AccessRights;
import org.n52.eventing.wv.security.UserSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionsServiceImpl extends BaseService implements SubscriptionsService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionsServiceImpl.class);

    @Autowired
    HibernateDatabaseConnection hibernateConnection;

    @Autowired
    UserSecurityService userSecurityService;

    @Autowired
    AccessRights accessRights;
    private final DeliveryMethodInstance defaultDeliveryMethod;

    public SubscriptionsServiceImpl() {
        this.defaultDeliveryMethod = new DeliveryMethodInstance(new EmailDeliveryProviderImpl().getIdentifier(),
                Collections.emptyMap());
    }



    @Override
    public boolean hasSubscription(String id) {
        int idInt = super.parseId(id);

        WvUser user;
        try {
            user = super.resolveUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            return false;
        }

        Session session = hibernateConnection.createSession();
        SubscriptionDao dao = new HibernateSubscriptionDao(session);
        try {
            Optional<WvSubscription> sub = dao.retrieveById(idInt);
            return sub.isPresent() && accessRights.canSeeSubscription(user, sub.get());
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
        finally {
            session.close();
        }
        return false;
    }

    @Override
    public List<SubscriptionInstance> getSubscriptions() {
        WvUser user;
        try {
            user = super.resolveUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            return Collections.emptyList();
        }

        Session session = hibernateConnection.createSession();
        SubscriptionDao dao = new HibernateSubscriptionDao(session);
        try {
            List<WvSubscription> subs = dao.retrieve(null);
            return subs.stream()
                    .filter(s -> accessRights.canSeeSubscription(user, s))
                    .map((WvSubscription t) -> {
                return wrapSubscription(t);
            }).collect(Collectors.toList());
        }
        catch (DatabaseException | NumberFormatException e) {
            LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
        finally {
            session.close();
        }

        return Collections.emptyList();
    }

    @Override
    public SubscriptionInstance getSubscription(String id) throws UnknownSubscriptionException {
        WvUser user;
        try {
            user = super.resolveUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            throw new UnknownSubscriptionException("Could not find subscription with id: "+id);
        }

        Session session = hibernateConnection.createSession();
        SubscriptionDao dao = new HibernateSubscriptionDao(session);
        try {
            Optional<WvSubscription> sub = dao.retrieveById(Integer.parseInt(id));
            if (!sub.isPresent() || !accessRights.canSeeSubscription(user, sub.get())) {
                throw new UnknownSubscriptionException("Could not find subscription with id: "+id);
            }
            return wrapSubscription(sub.get());
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
        finally {
            session.close();
        }

        throw new UnknownSubscriptionException("Could not find subscription with id: "+id);
    }

    @Override
    public void addSubscription(String subId, SubscriptionInstance subscription) {
        LOG.debug("No operation required, everything already done by "+FilterLogicImpl.class.getSimpleName());
    }

    @Override
    public SubscriptionInstance updateEndOfLife(String id, DateTime eol) throws UnknownSubscriptionException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SubscriptionInstance updateStatus(String id, boolean enabled) throws UnknownSubscriptionException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void remove(String id) throws UnknownSubscriptionException {
        int idInt = super.parseId(id);

        WvUser user;
        try {
            user = super.resolveUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            throw new UnknownSubscriptionException("Could not find subscription with id: "+id);
        }

        Session session = hibernateConnection.createSession();
        SubscriptionDao dao = new HibernateSubscriptionDao(session);
        Optional<WvSubscription> sub = dao.retrieveById(idInt);

        if (!sub.isPresent() || !accessRights.canManageSubscription(user, sub.get())) {
            throw new UnknownSubscriptionException("Could not find subscription with id: "+id);
        }

        try {
            dao.remove(sub.get());
        } catch (DatabaseException ex) {
            throw new RuntimeException("Could not remove subscription", ex);
        }
    }

    private SubscriptionInstance wrapSubscription(WvSubscription sub) {
        String desc = String.format("Subscrition for rule '%s'", sub.getRule().getId());
        SubscriptionInstance result = new SubscriptionInstance(Integer.toString(sub.getId()),
                desc,
                desc);
        result.setDeliveryMethods(Collections.singletonList(defaultDeliveryMethod));
        return result;
    }

}
