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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.RequestContext;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.eventlog.EventHolder;
import org.n52.eventing.rest.eventlog.EventLogStore;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.eventing.rest.users.User;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.security.SecurityService;
import org.n52.eventing.wv.dao.hibernate.HibernateEventDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.i18n.I18nProvider;
import org.n52.eventing.wv.model.WvEvent;
import org.n52.eventing.wv.model.WvEventHolder;
import org.n52.eventing.wv.model.WvUser;
import org.n52.eventing.wv.security.AccessRights;
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
    private I18nProvider i18n;

    @Autowired
    private HibernateDatabaseConnection hdc;

    @Autowired
    private SubscriptionsServiceImpl subscriptionService;

    @Override
    public void addEvent(SubscriptionInstance sub, EventHolder eh, int maximumCapacity) {
        LOG.debug("NoOp addEvent()");
    }

    @Override
    public Collection<EventHolder> getAllEvents() {
        return getAllEvents(null);
    }

    @Override
    public Collection<EventHolder> getAllEvents(Pagination pagination) {
        RequestContext context = RequestContext.retrieveFromThreadLocal();

        try (Session session = hdc.createSession()) {
            HibernateEventDao dao = new HibernateEventDao(session);

            Map<String, String[]> filter;
            try {
                filter = createFilter(context);
            } catch (NotAuthenticatedException ex) {
                LOG.warn("Not logged in", ex.getMessage());
                LOG.debug(ex.getMessage(), ex);
                return Collections.emptyList();
            }

            String[] latest = context.getParameters().get("latest");
            boolean latestBool = false;
            if (latest != null && latest.length > 0) {
                latestBool = Boolean.parseBoolean(latest[0]);
            }

            final List<WvEvent> result;
            if (latestBool) {
                Pagination latestPagination = new Pagination(0, 1);
                result = new ArrayList<>();
                String[] subs = filter.get("subscription");
                Stream.of(subs).forEach(sub -> {
                    Map<String, String[]> singleSub = Collections.singletonMap("subscription", new String[] {sub});
                    result.addAll(dao.retrieveWithFilter(singleSub, latestPagination));
                });
            }
            else {
                result = filter.isEmpty() ? dao.retrieve(pagination) : dao.retrieveWithFilter(filter, pagination);
            }

            return result.stream()
                    .map((WvEvent e) -> wrapEventBrief(e, null))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<EventHolder> getEventsForSubscription(SubscriptionInstance subscription, Pagination pagination) {
        try (Session session = hdc.createSession()) {
            int idInt = super.parseId(subscription.getId());
            HibernateEventDao dao = new HibernateEventDao(session);
            List<WvEvent> result = dao.retrieveForSubscription(idInt, pagination);
            return result.stream()
                    .map((WvEvent e) -> wrapEventBrief(e, null))
                    .collect(Collectors.toList());
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }

        return Collections.emptyList();
    }



    @Override
    public Collection<EventHolder> getEventsForSubscription(SubscriptionInstance subscription) {
        return getEventsForSubscription(subscription, null);
    }

    @Override
    public Optional<EventHolder> getSingleEvent(String eventId, RequestContext context) {
        try (Session session = hdc.createSession()) {
            int idInt = super.parseId(eventId);
            HibernateEventDao dao = new HibernateEventDao(session);
            Optional<WvEvent> result = dao.retrieveById(idInt);

            if (result.isPresent()) {
                WvEvent event = result.get();
                Hibernate.initialize(event.getRule());
                return Optional.of(wrapEventBrief(event, context));
            }

            return Optional.empty();
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }

        return Optional.empty();
    }

    private EventHolder wrapEventBrief(WvEvent e, RequestContext context) {
        String matchTemplate = i18n.getString("event.match");
        String label = String.format(matchTemplate, e.getRule());
        WvEventHolder holder = new WvEventHolder(Integer.toString(e.getId()),
                new DateTime(e.getTimestamp()),
                null, label, Optional.empty());
        Map<String, Object> props = new HashMap<>();
        props.put("value", e.getValue());
        props.put("previousValue", e.getPreviousValue());
        props.put("previousTimestamp", new DateTime(e.getPreviousTimestamp()));
        props.put("template", e.getRule().getId());
        holder.setData(props);
        holder.setCreated(new DateTime(e.getCreated().getTime()));

        if (context != null) {
            holder.setSeries(createIdHrefMap(context.getBaseApiUrl(), e.getRule().getSeries().getId(), UrlSettings.PUBLICATIONS_RESOURCE));
            holder.setRule(createIdHrefMap(context.getBaseApiUrl(), e.getRule().getId(), UrlSettings.TEMPLATES_RESOURCE));
        }

        return holder;
    }

    private Map<String, String> createIdHrefMap(String baseApiUrl, int resourceId, String targetResource) {
        Map<String, String> result = new HashMap<>();
        result.put("id", Integer.toString(resourceId));
        result.put("href", String.format("%s/%s/%s", baseApiUrl, targetResource, resourceId));
        return result;
    }

    private Map<String, String[]> createFilter(RequestContext context) throws NotAuthenticatedException {
        Map<String, String[]> params = context.getParameters();
        Map<String, String[]> filter = new HashMap<>();
        if (params != null && !params.isEmpty()) {
            String[] publications = params.get("publication");
            if (publications != null && publications.length > 0) {
                filter.put("publication", publications[0].split(","));
            }

            String[] subscriptions = params.get("subscription");
            List<SubscriptionInstance> userSubscriptions = subscriptionService.getSubscriptions(null);
            if (subscriptions != null && subscriptions.length > 0) {
                List<String> candidates = Arrays.asList(subscriptions[0].split(","));

                WvUser u = super.resolveUser();
                List<String> filtered = userSubscriptions.stream()
                        .map(s -> s.getId())
                        .filter(s -> candidates.contains(s))
                        .collect(Collectors.toList());

                filter.put("subscription", filtered.toArray(new String[filtered.size()]));
            }
            else {
                List<String> mapped = userSubscriptions.stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList());
                filter.put("subscription", mapped.toArray(new String[mapped.size()]));
            }
        }

        return filter;
    }



}
