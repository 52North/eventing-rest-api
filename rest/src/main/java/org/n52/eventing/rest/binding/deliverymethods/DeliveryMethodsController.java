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
package org.n52.eventing.rest.binding.deliverymethods;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.n52.eventing.rest.InvalidPaginationException;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.PaginationFactory;
import org.n52.eventing.rest.QueryResult;
import org.n52.eventing.rest.RequestContext;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.ResourceCollectionWithMetadata;
import org.n52.eventing.rest.binding.ResourceNotFoundException;
import org.n52.eventing.rest.model.DeliveryMethodDefinition;
import org.n52.eventing.rest.deliverymethods.UnknownDeliveryMethodException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @Autowired
    private RequestContext context;

    @Autowired
    private PaginationFactory pageFactory;

    @RequestMapping("")
    public ResourceCollectionWithMetadata<ResourceCollection> getDeliveryMethods() throws IOException, URISyntaxException, InvalidPaginationException {
        String fullUrl = context.getFullUrl();
        List<ResourceCollection> list = new ArrayList<>();

        RequestContext.storeInThreadLocal(context);
        Pagination page = pageFactory.fromQuery(context.getParameters());

        try {
            this.dao.getDeliveryMethods().stream().forEach(dm -> {
                list.add(ResourceCollection.createResource(dm.getId())
                    .withLabel(dm.getLabel())
                    .withDescription(dm.getDescription())
                    .withHref(String.format("%s/%s", fullUrl, dm.getId())));
            });

            if (list.isEmpty()) {
                return new ResourceCollectionWithMetadata<>(new QueryResult<>(Collections.emptyList(), 0), page);
            }
        }
        finally {
            RequestContext.removeThreadLocal();
        }


        QueryResult<ResourceCollection> result = new QueryResult<>(list, list.size());
        return new ResourceCollectionWithMetadata<>(result.getResult(), new ResourceCollectionWithMetadata.Metadata(result.getTotalHits(), pageFactory.defaultPagination()));
    }

    @RequestMapping("/{item}")
    public DeliveryMethodDefinition getDeliveryMethod(@PathVariable("item") String id) throws ResourceNotAvailableException, ResourceNotFoundException {
        if (this.dao.hasDeliveryMethod(id)) {
            RequestContext.storeInThreadLocal(context);

            try {
                DeliveryMethodDefinition method = this.dao.getDeliveryMethod(id);
                return method;
            } catch (UnknownDeliveryMethodException ex) {
                throw new ResourceNotAvailableException(ex.getMessage(), ex);
            } finally {
                RequestContext.removeThreadLocal();
            }
        }

        throw new ResourceNotFoundException("Delivery method not available: "+id);
    }

}
