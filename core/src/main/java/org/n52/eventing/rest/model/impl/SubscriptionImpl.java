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
package org.n52.eventing.rest.model.impl;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodInstance;
import org.n52.eventing.rest.model.Subscription;
import org.n52.eventing.rest.templates.TemplateInstance;
import org.n52.eventing.rest.users.User;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionImpl implements Subscription {

    private String id;
    private String label;
    private String description;
    private DateTime created;
    private DateTime modified;
    private User user;
    private String publicationId;
    private TemplateInstance template;
    private List<DeliveryMethodInstance> deliveryMethods = new ArrayList<>();
    private Boolean enabled;
    private DateTime endOfLife;
    private Boolean expired;
    private Object details;
    private String href;

    public SubscriptionImpl() {
    }

    public SubscriptionImpl(String id, String label, String description) {
        this();
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getPublicationId() {
        return publicationId;
    }

    @Override
    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public DateTime getEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(DateTime endOfLife) {
        this.endOfLife = endOfLife;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    @Override
    public TemplateInstance getNotificationInstance() {
        return template;
    }

    public void setTemplate(TemplateInstance template) {
        this.template = template;
    }

    public List<DeliveryMethodInstance> getDeliveryMethods() {
        return deliveryMethods;
    }

    public void setDeliveryMethods(List<DeliveryMethodInstance> deliveryMethods) {
        this.deliveryMethods = deliveryMethods;
    }

    public void addDeliveryMethod(DeliveryMethodInstance deliveryMethod) {
        this.deliveryMethods.add(deliveryMethod);
    }

    public DateTime getCreated() {
        return created;
    }

    @Override
    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getModified() {
        return modified;
    }

    @Override
    public void setModified(DateTime modified) {
        this.modified = modified;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public String getHref() {
        return href;
    }

    @Override
    public void setHref(String href) {
        this.href = href;
    }

}
