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
package org.n52.eventing.rest.deliverymethods.email;

import java.util.Collections;
import java.util.Map;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.DeliveryParameter;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class EmailDeliveryProvider implements DeliveryProvider {

    private static final String ID = "email";

    @Override
    public boolean supportsDeliveryIdentifier(String id) {
        return ID.equals(id);
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public String getAbstract() {
        return "Email delivery";
    }

    @Override
    public DeliveryEndpoint createDeliveryEndpoint(DeliveryDefinition def) throws UnsupportedDeliveryDefinitionException {
        return new EmailDeliveryEndpoint(def.getLocation());
    }


    @Override
    public DeliveryParameter[] getParameters() {
        DeliveryParameter[] params = new DeliveryParameter[3];
        String namespace = "eventing-rest-api";
        params[0] = new DeliveryParameter("text", namespace, "to", null);
        params[1] = new DeliveryParameter("text", namespace, "cc", null);
        params[2] = new DeliveryParameter("text", namespace, "subject", "[Notification] Rule matched");
        return params;
    }

    @Override
    public Map<? extends String, ? extends String> getNamespacePrefixMap() {
        return Collections.emptyMap();
    }

}
