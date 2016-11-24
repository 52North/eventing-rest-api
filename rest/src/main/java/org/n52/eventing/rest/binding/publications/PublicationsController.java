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

package org.n52.eventing.rest.binding.publications;

import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.binding.EmptyArrayModel;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.security.SecurityService;
import org.n52.eventing.rest.publications.Publication;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.n52.eventing.rest.security.SecurityRights;
import org.n52.eventing.rest.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.n52.eventing.rest.publications.PublicationsService;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.PUBLICATIONS_RESOURCE,
        produces = {"application/json"})
public class PublicationsController {

    @Autowired
    private PublicationsService dao;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SecurityRights rights;


    @RequestMapping("")
    public ModelAndView getPublications(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException, NotAuthenticatedException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();

        List<ResourceCollection> pubs = createPublications(fullUrl);

        if (pubs.isEmpty()) {
            return EmptyArrayModel.create();
        }

        return new ModelAndView().addObject(pubs);
    }

    private List<ResourceCollection> createPublications(String fullUrl) throws NotAuthenticatedException {
        List<ResourceCollection> pubs = new ArrayList<>();

        User user = securityService.resolveCurrentUser();

        this.dao.getPublications().stream().forEach(p -> {
            String pubId = p.getId();

            if (!rights.canSeePublication(user, p)) {
                return;
            }

            pubs.add(ResourceCollection.createResource(pubId)
                    .withLabel(p.getLabel())
                    .withDescription(p.getDescription())
                    .withHref(String.format("%s/%s", fullUrl, pubId)));
        });

        return pubs;
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getPublication(@RequestParam(required = false) MultiValueMap<String, String> query,
            @PathVariable("item") String id)
            throws IOException, URISyntaxException, ResourceNotAvailableException, NotAuthenticatedException {

        if (!this.dao.hasPublication(id)) {
            throw new ResourceNotAvailableException("The publication is not available: "+id);
        }

        try {
            User user = securityService.resolveCurrentUser();

            Publication pub = this.dao.getPublication(id);

            if (!rights.canSeePublication(user, pub)) {
                throw new ResourceNotAvailableException("The publication is not available: "+id);
            }

            return new ModelAndView().addObject(pub);
        } catch (UnknownPublicationsException ex) {
            throw new ResourceNotAvailableException(ex.getMessage(), ex);
        }
    }

}
