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

package org.n52.eventing.rest.binding.deliverymethods;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.binding.EmptyArrayModel;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodDefinition;
import org.n52.eventing.rest.deliverymethods.UnknownDeliveryMethodException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsService;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.DELIVERY_METHODS_RESOURCE,
        produces = {"application/json"})
public class DeliveryMethodsController {

    @Autowired
    private DeliveryMethodsService dao;


    @RequestMapping("")
    public ModelAndView getDeliveryMethods() throws IOException, URISyntaxException, NotAuthenticatedException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        List<ResourceCollection> list = new ArrayList<>();

        this.dao.getDeliveryMethods().stream().forEach(dm -> {
            list.add(ResourceCollection.createResource(dm.getId())
                .withLabel(dm.getLabel())
                .withDescription(dm.getDescription())
                .withHref(String.format("%s/%s", fullUrl, dm.getId())));
        });

        if (list.isEmpty()) {
            return EmptyArrayModel.create();
        }

        return new ModelAndView().addObject(list);
    }

    @RequestMapping("/{item}")
    public DeliveryMethodDefinition getDeliveryMethod(@PathVariable("item") String id) throws ResourceNotAvailableException, NotAuthenticatedException {
        if (this.dao.hasDeliveryMethod(id)) {
            try {
                DeliveryMethodDefinition method = this.dao.getDeliveryMethod(id);
                return method;
            } catch (UnknownDeliveryMethodException ex) {
                throw new ResourceNotAvailableException(ex.getMessage(), ex);
            }
        }

        throw new ResourceNotAvailableException("Delivery method not available: "+id);
    }

}
