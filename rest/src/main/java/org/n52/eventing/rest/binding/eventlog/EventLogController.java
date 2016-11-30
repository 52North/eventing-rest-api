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

package org.n52.eventing.rest.binding.eventlog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.rest.eventlog.EventHolder;
import org.n52.eventing.rest.eventlog.EventLogStore;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.eventing.rest.subscriptions.UnknownSubscriptionException;
import org.n52.subverse.delivery.Streamable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.n52.eventing.rest.subscriptions.SubscriptionsService;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.EVENTLOG_RESOURCE,
        produces = {"application/json"})
public class EventLogController {

    @Autowired
    private SubscriptionsService subDao;

    @Autowired
    private EventLogStore store;


    @RequestMapping("")
    public Collection<EventHolderView> getAllEvents(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException, NotAuthenticatedException {
        final String fullUrl = RequestUtils.resolveFullRequestUrl();

        return store.getAllEvents().stream()
                .map((EventHolder t) -> {
                    String id = t.getId();
                    return new EventHolderView(id, t.getTime(),
                            t.subscription() != null ? t.subscription().getId() : null,
                            t.getLabel(),
                            String.format("%s/%s", fullUrl, id));
                })
                .collect(Collectors.toList());
    }

    public Collection<EventHolderView> getEventsForSubscription(String subId)
            throws IOException, URISyntaxException, NotAuthenticatedException, UnknownSubscriptionException {
        final String fullUrl = RequestUtils.resolveFullRequestUrl();

        SubscriptionInstance subscription = subDao.getSubscription(subId);

        return store.getEventsForSubscription(subscription).stream()
                .map((EventHolder t) -> {
                    String id = t.getId();
                    EventHolderView holderView = new EventHolderView(id, t.getTime(),
                            t.subscription() != null ? t.subscription().getId() : null,
                            t.getLabel(),
                            String.format("%s/%s", fullUrl, id));
                    holderView.setData(t.getData());
                    return holderView;
                })
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{eventId}", method = GET)
    public ModelAndView getSingleEvent(@PathVariable("eventId") String eventId)
            throws IOException, URISyntaxException, NotAuthenticatedException, UnknownSubscriptionException, ResourceNotAvailableException {
        Optional<EventHolder> result = retrieveSingleEvent(eventId);

        if (result.isPresent()) {
            final String fullUrl = RequestUtils.resolveFullRequestUrl();
            ModelAndView mav = new ModelAndView();
            mav.addObject("event", result.get());
            mav.addObject("href", fullUrl.concat("/content"));
            return mav;
        }

        throw new ResourceNotAvailableException("Could not find event");
    }
    
    public ModelAndView getSingleEventForSubscription(String subId, String eventId)
            throws IOException, URISyntaxException, NotAuthenticatedException, UnknownSubscriptionException, ResourceNotAvailableException {
        return getSingleEvent(eventId);
    }


    private Optional<EventHolder> retrieveSingleEvent(String eventId) throws NotAuthenticatedException, UnknownSubscriptionException {
        Optional<EventHolder> result = store.getSingleEvent(eventId);
        return result;
    }

    @RequestMapping(value = "/{eventId}/content", method = GET)
    public void getSingleEventContent(@PathVariable("eventId") String eventId)
            throws IOException, URISyntaxException, NotAuthenticatedException, UnknownSubscriptionException, ResourceNotAvailableException {
        Optional<EventHolder> holder = retrieveSingleEvent(eventId);

        if (!holder.isPresent()) {
            throw new ResourceNotAvailableException("Could not find event");
        }

        Optional<Streamable> streamable = holder.get().streamableObject();
        if (streamable.isPresent()) {
            Streamable obj = streamable.get();
            HttpServletResponse resp = RequestUtils.resolveResponseObject();
            String ct = obj.getContentType();
            resp.setContentType(ct);
            resp.setStatus(200);

            InputStream is = obj.asStream();
            try (ServletOutputStream os = resp.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = is.read(buffer)) > 0) {
                    os.write(buffer, 0, count);
                    os.flush();
                }
            }
        }
        else {
            throw new ResourceNotAvailableException("No content for this event available");
        }
    }

    public static class EventHolderView {

        private final String id;
        private final DateTime time;
        private final String subscriptionId;
        private final String label;
        private final String href;
        private Object data;

        public EventHolderView(String id, DateTime time, String subscriptionId, String label, String href) {
            this.id = id;
            this.time = time;
            this.subscriptionId = subscriptionId;
            this.label = label;
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public DateTime getTime() {
            return time;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public String getLabel() {
            return label;
        }

        public String getHref() {
            return href;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }
        
    }
}
