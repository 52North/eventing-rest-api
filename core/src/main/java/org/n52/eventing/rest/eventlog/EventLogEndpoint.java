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
package org.n52.eventing.rest.eventlog;

import org.n52.eventing.rest.model.impl.EventHolderImpl;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.n52.eventing.rest.model.impl.SubscriptionImpl;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.Streamable;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class EventLogEndpoint implements DeliveryEndpoint {

    private final int maximumCapacity;
    private final SubscriptionImpl subscription;
    private final EventLogStore store;
    private final AtomicInteger count = new AtomicInteger(1);

    public EventLogEndpoint(int maximumCapacity, SubscriptionImpl subscription, EventLogStore store) {
        this.maximumCapacity = maximumCapacity;
        this.subscription = subscription;
        this.store = store;
    }


    @Override
    public void deliver(Optional<Streamable> o, boolean asRaw) {
        EventHolderImpl eh = new EventHolderImpl(String.format("%s_match_%s", subscription.getId(), count.getAndIncrement()),
                new DateTime(), subscription, null, o);
        this.store.addEvent(subscription, eh, maximumCapacity);
    }

    @Override
    public String getEffectiveLocation() {
        return "/eventLog";
    }

    @Override
    public void destroy() {
    }

}
