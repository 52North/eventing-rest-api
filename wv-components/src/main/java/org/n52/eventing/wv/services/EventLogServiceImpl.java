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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.eventing.rest.eventlog.EventHolder;
import org.n52.eventing.rest.eventlog.EventLogStore;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.eventing.wv.dao.hibernate.HibernateEventDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.WvEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class EventLogServiceImpl extends BaseService implements EventLogStore {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogServiceImpl.class);

    @Autowired
    private HibernateDatabaseConnection hdc;

    @Override
    public void addEvent(SubscriptionInstance sub, EventHolder eh, int maximumCapacity) {
        LOG.debug("NoOp addEvent()");
    }

    @Override
    public Collection<EventHolder> getAllEvents() {
        try (Session session = hdc.createSession()) {
            HibernateEventDao dao = new HibernateEventDao(session);
            List<WvEvent> result = dao.retrieve(null);
            return result.stream()
                    .map((WvEvent e) -> wrapEventBrief(e))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<EventHolder> getEventsForSubscription(SubscriptionInstance subscription) {
        try (Session session = hdc.createSession()) {
            int idInt = super.parseId(subscription.getId());
            HibernateEventDao dao = new HibernateEventDao(session);
            List<WvEvent> result = dao.retrieveForSubscription(idInt);
            return result.stream()
                    .map((WvEvent e) -> wrapEventBrief(e))
                    .collect(Collectors.toList());
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }

        return Collections.emptyList();
    }

    @Override
    public Optional<EventHolder> getSingleEvent(String eventId) {
        try (Session session = hdc.createSession()) {
            int idInt = super.parseId(eventId);
            HibernateEventDao dao = new HibernateEventDao(session);
            Optional<WvEvent> result = dao.retrieveById(idInt);
            return Optional.ofNullable(result.isPresent() ? wrapEventBrief(result.get()) : null);
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }

        return Optional.empty();
    }



    private EventHolder wrapEventBrief(WvEvent e) {
        String label = String.format("Match for rule: '%s'", e.getRule());
        EventHolder holder = new EventHolder(Integer.toString(e.getId()),
                new DateTime(e.getTimestamp()),
                null, label, null);
        Map<String, Object> props = new HashMap<>();
        props.put("value", e.getValue());
        props.put("previousValue", e.getPreviousValue());
        props.put("previousTimestamp", new DateTime(e.getPreviousTimestamp()));
        holder.setData(props);
        return holder;
    }

}
