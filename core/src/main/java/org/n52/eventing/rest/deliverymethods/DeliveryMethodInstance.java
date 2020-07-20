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
package org.n52.eventing.rest.deliverymethods;

import java.util.Map;
import org.n52.eventing.rest.parameters.ParameterInstance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@JsonPropertyOrder({"id", "href", "label", "parameters"})
public class DeliveryMethodInstance {

    private final String id;
    private final Map<String, ParameterInstance> parameters;
    private Object details;
    private String href;

    public DeliveryMethodInstance() {
        this(null, null);
    }

    public DeliveryMethodInstance(String id, Map<String, ParameterInstance> parameters) {
        this(id, parameters, null);
    }

    public DeliveryMethodInstance(String id, Map<String, ParameterInstance> parameters,  String href) {
        this.id = id;
        this.parameters = parameters;
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public Map<String, ParameterInstance> getParameters() {
        return parameters;
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

    public void setHref(String href) {
        this.href = href;
    }

}
