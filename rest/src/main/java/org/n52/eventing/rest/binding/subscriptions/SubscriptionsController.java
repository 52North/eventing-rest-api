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
package org.n52.eventing.rest.binding.subscriptions;

import com.fasterxml.jackson.annotation.JsonView;
import org.n52.eventing.rest.subscriptions.SubscriptionUpdate;
import org.n52.eventing.rest.binding.BaseController;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.binding.exception.ResourceNotFoundException;
import org.n52.eventing.rest.binding.exception.concrete.ResourceWithIdNotFoundException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.n52.eventing.rest.InvalidPaginationException;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.PaginationFactory;
import org.n52.eventing.rest.QueryResult;
import org.n52.eventing.rest.RequestContext;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.ResourceCollectionWithMetadata;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.security.SecurityService;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.n52.eventing.rest.subscriptions.SubscriptionManager;
import org.n52.eventing.rest.model.Subscription;
import org.n52.eventing.rest.model.views.Views;
import org.n52.eventing.rest.subscriptions.UnknownSubscriptionException;
import org.n52.eventing.rest.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
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
public class SubscriptionsController extends BaseController{

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionsController.class);

    @Autowired
    private SubscriptionsService dao;

    @Autowired
    private SubscriptionManager manager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RequestContext context;

    @Autowired
    private PaginationFactory pageFactory;

    @JsonView(Views.SubscriptionExpanded.class)
    @RequestMapping("")
    public ResourceCollectionWithMetadata<Subscription> getSubscriptions()
            throws IOException, URISyntaxException, InvalidPaginationException {
        Map<String, String[]> query = context.getParameters();
        Pagination p = pageFactory.fromQuery(query);

        String fullUrl = context.getFullUrl();

        return retrieveSubscriptions(fullUrl, p);
    }

    private ResourceCollectionWithMetadata<Subscription> retrieveSubscriptions(String fullUrl, Pagination p) {
        RequestContext.storeInThreadLocal(context);

        try {
            QueryResult<Subscription> result = this.dao.getSubscriptions(p);
            result.setResult(result.getResult().stream()
                .map((Subscription si) -> {
                    si.setHref(String.format("%s/%s", fullUrl, si.getId()));
                    return si;
                })
                .collect(Collectors.toList()));
            return new ResourceCollectionWithMetadata<>(result.getResult(), new ResourceCollectionWithMetadata.Metadata(result.getTotalHits(), p));
        }
        finally {
            RequestContext.removeThreadLocal();
        }

    }

    @JsonView(Views.SubscriptionExpanded.class)
    @RequestMapping(value = "/{item}", method = GET)
    public Subscription getSubscription(@PathVariable("item") String id)
            throws IOException, URISyntaxException, ResourceNotAvailableException {
        RequestContext.storeInThreadLocal(context);

        if (!this.dao.hasSubscription(id)) {
            throw new ResourceNotAvailableException("The subscription is not available: "+id);
        }

        try {
            Subscription sub = this.dao.getSubscription(id);
            return sub;
        } catch (UnknownSubscriptionException ex) {
            throw new ResourceNotAvailableException(ex.getMessage(), ex);
        }
        finally {
            RequestContext.removeThreadLocal();
        }
    }


    @RequestMapping(value = "", method = POST)
    public ModelAndView subscribe(@RequestBody Subscription subDef) throws InvalidSubscriptionException, ResourceNotFoundException {
        final User user;
        try {
            user = securityService.resolveCurrentUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            LOG.trace(ex.getMessage(), ex);
            throw new ResourceWithIdNotFoundException("me");
        }

        RequestContext.storeInThreadLocal(context);

        try {
            String subId = this.manager.subscribe(subDef, user);
            ModelAndView result = new ModelAndView();

            Map<String, String> map = new HashMap<>();
            map.put("id", subId);
            map.put("href", String.format("%s/%s",
                    context.getFullUrl(),
                    subId));
            result.addObject(map);
            return result;
        }
        finally {
            RequestContext.removeThreadLocal();
        }
    }

    @RequestMapping(value = "/{item}", method = PUT)
    public ResponseEntity<?> updateSubscription(@RequestBody SubscriptionUpdate subDef,
            @PathVariable("item") String id) throws InvalidSubscriptionException, ResourceNotFoundException {
        subDef.setId(id);
        final User user;
        try {
            user = securityService.resolveCurrentUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            LOG.trace(ex.getMessage(), ex);
            throw new ResourceWithIdNotFoundException(id);
        }

        this.manager.updateSubscription(subDef, user);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{item}", method = DELETE)
    public ResponseEntity<?> remove(@PathVariable("item") String id) throws InvalidSubscriptionException, ResourceNotFoundException {
        final User user;
        try {
            user = securityService.resolveCurrentUser();
        } catch (NotAuthenticatedException ex) {
            LOG.warn(ex.getMessage());
            LOG.trace(ex.getMessage(), ex);
            throw new ResourceWithIdNotFoundException(id);
        }

        this.manager.removeSubscription(id, user);

        return ResponseEntity.accepted().build();
    }

}
