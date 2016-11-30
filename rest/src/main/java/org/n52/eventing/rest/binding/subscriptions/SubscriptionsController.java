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

package org.n52.eventing.rest.binding.subscriptions;

import org.n52.eventing.rest.binding.EmptyArrayModel;
import org.n52.eventing.rest.subscriptions.SubscriptionUpdateInstance;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.binding.eventlog.EventLogController;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.security.SecurityService;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.n52.eventing.rest.subscriptions.SubscriptionManager;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.eventing.rest.subscriptions.UnknownSubscriptionException;
import org.n52.eventing.rest.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.n52.eventing.rest.subscriptions.SubscriptionsService;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.SUBSCRIPTIONS_RESOURCE,
        produces = {"application/json"})
public class SubscriptionsController {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionsController.class);

    @Autowired
    private SubscriptionsService dao;

    @Autowired
    private SubscriptionManager manager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EventLogController eventLogController;

    @RequestMapping("")
    public ModelAndView getSubscriptions(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException, NotAuthenticatedException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();

        List<ResourceCollection> subs = retrieveSubscriptions(fullUrl);

        if (subs.isEmpty()) {
            return EmptyArrayModel.create();
        }

        return new ModelAndView().addObject(subs);
    }

    private List<ResourceCollection> retrieveSubscriptions(String fullUrl) throws NotAuthenticatedException {
        List<ResourceCollection> pubs = new ArrayList<>();

        this.dao.getSubscriptions().stream().forEach(s -> {
            String pubId = s.getId();
            pubs.add(ResourceCollection.createResource(pubId)
                    .withLabel(s.getLabel())
                    .withDescription(s.getDescription())
                    .withUserId(s.getUser() != null ? s.getUser().getId() : null)
                    .withHref(String.format("%s/%s", fullUrl, pubId)));
        });

        return pubs;
    }

    @RequestMapping(value = "/{item}", method = GET)
    public SubscriptionInstance getSubscription(@RequestParam(required = false) MultiValueMap<String, String> query,
            @PathVariable("item") String id)
            throws IOException, URISyntaxException, ResourceNotAvailableException, NotAuthenticatedException {

        if (!this.dao.hasSubscription(id)) {
            throw new ResourceNotAvailableException("The subscription is not available: "+id);
        }

        try {
            SubscriptionInstance sub = this.dao.getSubscription(id);
            return sub;
        } catch (UnknownSubscriptionException ex) {
            throw new ResourceNotAvailableException(ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "/{subId}/events", method = GET)
    public Collection<EventLogController.EventHolderView> getSubscriptionEvents(@PathVariable("subId") String subId)
            throws IOException, URISyntaxException, NotAuthenticatedException, UnknownSubscriptionException, ResourceNotAvailableException {
        return eventLogController.getEventsForSubscription(subId);
    }


    @RequestMapping(value = "", method = POST)
    public ModelAndView subscribe(@RequestBody SubscriptionInstance subDef) throws InvalidSubscriptionException, NotAuthenticatedException {
        final User user = securityService.resolveCurrentUser();

        String subId = this.manager.subscribe(subDef, user);

        return new ModelAndView().addObject(Collections.singletonMap("id", subId));
    }

    @RequestMapping(value = "/{item}", method = PUT)
    public ResponseEntity<?> updateSubscription(@RequestBody SubscriptionUpdateInstance subDef,
            @PathVariable("item") String id) throws InvalidSubscriptionException, NotAuthenticatedException {
        subDef.setId(id);
        final User user = securityService.resolveCurrentUser();

        this.manager.updateSubscription(subDef, user);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{item}", method = DELETE)
    public ResponseEntity<?> remove(@PathVariable("item") String id) throws InvalidSubscriptionException, NotAuthenticatedException {
        final User user = securityService.resolveCurrentUser();

        this.manager.removeSubscription(id, user);

        return ResponseEntity.accepted().build();
    }

}
