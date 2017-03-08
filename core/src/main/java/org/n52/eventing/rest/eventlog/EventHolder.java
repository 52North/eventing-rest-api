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

package org.n52.eventing.rest.eventlog;

import java.util.Optional;
import org.joda.time.DateTime;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.subverse.delivery.Streamable;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class EventHolder implements Comparable<EventHolder> {

    private final String id;
    private final DateTime timestamp;
    private final SubscriptionInstance subscription;
    private final String label;
    private transient final Optional<Streamable> streamable;
    private Object data;
    private String href;
    private String content;

    public EventHolder(String id, DateTime time, SubscriptionInstance subscription, String label, Optional<Streamable> streamable) {
        this.id = id;
        this.timestamp = time;
        this.subscription = subscription;
        this.label = label;
        this.streamable = streamable;
    }

    public String getId() {
        return id;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public SubscriptionInstance subscription() {
        return subscription;
    }

    public String getLabel() {
        return label;
    }

    public Optional<Streamable> streamableObject() {
        return streamable;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(EventHolder o) {
        if (this.timestamp == null) {
            return -1;
        }
        if (o.timestamp == null) {
            return 1;
        }

        return this.timestamp.compareTo(o.timestamp);
    }

}
