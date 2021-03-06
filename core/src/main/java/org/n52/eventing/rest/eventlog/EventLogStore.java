/*
 * Copyright (C) 2016-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.eventing.rest.eventlog;

import org.n52.eventing.rest.model.EventHolder;
import java.util.Collection;
import java.util.Optional;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.QueryResult;
import org.n52.eventing.rest.RequestContext;
import org.n52.eventing.rest.model.Subscription;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface EventLogStore {

    /**
     * adds a new event to the store
     *
     * @param sub the subscription
     * @param eh the event holder instance
     * @param maximumCapacity the maximum capacity to store for this subscription ID
     */
    void addEvent(Subscription sub, EventHolder eh, int maximumCapacity);

    QueryResult<EventHolder> getAllEvents();

    default QueryResult<EventHolder> getAllEvents(Pagination pagination) {
        return getAllEvents();
    };

    QueryResult<EventHolder> getEventsForSubscription(Subscription subscription);

    default QueryResult<EventHolder> getEventsForSubscription(Subscription subscription, Pagination pagination) {
        return getEventsForSubscription(subscription);
    }

    public Optional<EventHolder> getSingleEvent(String eventId, RequestContext context);

}
