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

package org.n52.eventing.rest.binding;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE, produces = {"application/json"})
public class ResourcesController {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesController.class);

    @RequestMapping("")
    public ModelAndView getResources(@RequestParam(required = false) MultiValueMap<String, String> query) throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        return new ModelAndView().addObject(createResources(fullUrl));
    }

    private Map<String, String> createResources(String fullUrl) {
        LOG.info("Full URL: {}", fullUrl);
        Map<String, String> resources = new HashMap<>();

        resources.put(UrlSettings.PUBLICATIONS_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.PUBLICATIONS_RESOURCE));
        resources.put(UrlSettings.DELIVERY_METHODS_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.DELIVERY_METHODS_RESOURCE));
        resources.put(UrlSettings.SUBSCRIPTIONS_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.SUBSCRIPTIONS_RESOURCE));
        resources.put(UrlSettings.TEMPLATES_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.TEMPLATES_RESOURCE));

        return resources;
    }

}
