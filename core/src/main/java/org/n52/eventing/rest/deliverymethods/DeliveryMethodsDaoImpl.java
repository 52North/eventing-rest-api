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

package org.n52.eventing.rest.deliverymethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.n52.eventing.rest.Constructable;
import org.n52.eventing.rest.parameters.Parameter;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.DeliveryParameter;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.DeliveryProviderRepository;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DeliveryMethodsDaoImpl implements DeliveryMethodsDao, Constructable {

    @Autowired
    private DeliveryProviderRepository deliveryProviderRepository;

    private final Map<String, DeliveryMethod> methods = new HashMap<>();

    public DeliveryMethodsDaoImpl() {
    }

    @Override
    public List<DeliveryMethod> getDeliveryMethods() {
        return Collections.unmodifiableList(new ArrayList<>(methods.values()));
    }

    @Override
    public boolean hasDeliveryMethod(String id) {
        return methods.containsKey(id);
    }

    @Override
    public DeliveryMethod getDeliveryMethod(String id) throws UnknownDeliveryMethodException {
        if (hasDeliveryMethod(id)) {
            return methods.get(id);
        }

        throw new UnknownDeliveryMethodException("Unknown delivery method: "+id);
    }

    @Override
    public void construct() {
        this.deliveryProviderRepository.getProviders().stream().forEach(dp -> {
            DeliveryMethod method = new DeliveryMethod(dp.getIdentifier(), dp.getAbstract(),
                    dp.getAbstract(), mapParameters(dp.getParameters()));
            methods.put(dp.getIdentifier(), method);
        });
    }


    @Override
    public DeliveryEndpoint createDeliveryEndpoint(DeliveryMethodInstance deliveryMethod, String pubId) throws InvalidSubscriptionException {
        try {
            DeliveryDefinition definition = new DeliveryDefinition(deliveryMethod.getId(), null, pubId);
            DeliveryProvider provider = deliveryProviderRepository.getProvider(Optional.of(definition));

            if (provider == null) {
                throw new InvalidSubscriptionException("No delivery provider found for delivery: "+deliveryMethod.getId());
            }

            return provider.createDeliveryEndpoint(definition);
        } catch (UnsupportedDeliveryDefinitionException ex) {
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }
    }

    private Map<String, Parameter> mapParameters(DeliveryParameter[] parameters) {
        Map<String, Parameter> result = new HashMap<>(parameters.length);
        for (DeliveryParameter dp : parameters) {
            Parameter p = new Parameter(dp.getType(), dp.getElementName());
            p.setDefaultValue(dp.getValue());
            result.put(dp.getElementName(), p);
        }
        return result;
    }

}
