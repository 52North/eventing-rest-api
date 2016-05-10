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

package org.n52.eventing.rest.subscriptions;

import org.n52.eventing.rest.parameters.ParameterValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.eventing.rest.Constructable;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsDao;
import org.n52.eventing.rest.publications.PublicationsDao;
import org.n52.eventing.rest.templates.InstanceGenerator;
import org.n52.eventing.rest.parameters.Parameter;
import org.n52.eventing.rest.templates.Template;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.rest.users.UnknownUserException;
import org.n52.eventing.rest.users.User;
import org.n52.eventing.rest.users.UsersDao;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.engine.FilterEngine;
import org.n52.subverse.engine.SubscriptionRegistrationException;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.subscription.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionManagerImpl implements SubscriptionManager, Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionManagerImpl.class);

    @Autowired
    private SubscriptionsDao dao;

    @Autowired
    private PublicationsDao publicationsDao;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private DeliveryMethodsDao deliveryMethodsDao;

    @Autowired
    private TemplatesDao templatesDao;

    @Autowired
    private FilterEngine engine;

    private final InstanceGenerator filterInstanceGenerator = new InstanceGenerator();
    private final Map<String, Subscription> subscriptionToRuleMap = new HashMap<>();

    @Override
    public void construct() {
        LOG.info("Retrieveing persisted subscriptions...");
        AtomicInteger count = new AtomicInteger();
        this.dao.getSubscriptions().stream().forEach(s -> {
            LOG.info("Registering subscription {}", s.getId());
            try {
                internalSubscribe(s, templatesDao.getTemplate(s.getTemplateId()));
                count.getAndIncrement();
            } catch (UnknownTemplateException ex) {
                LOG.warn("Could not find template for subscription", ex);
            } catch (InvalidSubscriptionException ex) {
                LOG.warn("Could not create subscription", ex);
            }
        });
        LOG.info("Registered {} persisted subscriptions...", count);
    }

    @Override
    public String subscribe(SubscriptionDefinition subDef) throws InvalidSubscriptionException {
        throwExceptionOnNullOrEmpty(subDef.getPublicationId(), "publicationId");
        throwExceptionOnNullOrEmpty(subDef.getTemplateId(), "templateId");
        throwExceptionOnNullOrEmpty(subDef.getConsumer(), "consumer");
        throwExceptionOnNullOrEmpty(subDef.getDeliveryMethodId(), "deliveryMethodId");

        //TODO implement using Spring security
        User user;
        try {
            user = this.usersDao.getUser("dummy-user");
        } catch (UnknownUserException ex) {
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }

        String pubId = subDef.getPublicationId();
        if (!this.publicationsDao.hasPublication(pubId)) {
            throw new InvalidSubscriptionException("Publication unknown: "+pubId);
        }

        Template template;
        try {
            template = this.templatesDao.getTemplate(subDef.getTemplateId());
        } catch (UnknownTemplateException ex) {
            throw new InvalidSubscriptionException("Template unknown: "+pubId);
        }

        String deliveryMethodId = subDef.getDeliveryMethodId();
        if (!this.deliveryMethodsDao.hasDeliveryMethod(deliveryMethodId)) {
            throw new InvalidSubscriptionException("DeliveryMethod unknown: "+deliveryMethodId);
        }

        String consumer = subDef.getConsumer();
        String subId = UUID.randomUUID().toString();
        String desc = String.format("Subscription using template %s (created: %s)", template.getId(), new DateTime());
        String label = Optional.ofNullable(subDef.getLabel()).orElse(desc);

        SubscriptionRepresentation subscription = createSubscription(subId, label, desc, consumer,
                template, deliveryMethodId, pubId, user, subDef);

        //do the actual subscription part
        internalSubscribe(subscription, template);

        /*
        * finally add to the DAO
        */
        this.dao.addSubscription(subId, subscription);

        return subId;
    }

    private void internalSubscribe(SubscriptionRepresentation subscription, Template template) throws InvalidSubscriptionException {
        /*
        * resolve delivery endpoint
        */
        DeliveryEndpoint endpoint = this.deliveryMethodsDao.createDeliveryEndpoint(subscription.getDeliveryMethodId(),
                subscription.getConsumer(), subscription.getPublicationId());

        /*
        * register at engine
        */
        String filterInstance = this.filterInstanceGenerator.generateFilterInstance(
                template, subscription.getParameters());
        try {
            Subscription subverseSub = wrapToSubverseSubscription(subscription,
                    filterInstance, subscription.getPublicationId());
            this.engine.register(subverseSub, endpoint);

            /*
            * remember subverseSub for later removal
            */
            synchronized (this) {
                this.subscriptionToRuleMap.put(subscription.getId(), subverseSub);
            }
        } catch (SubscriptionRegistrationException ex) {
            LOG.warn("Could not register subscription at engine");
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }
    }

    private SubscriptionRepresentation createSubscription(String subId, String label, String desc, String consumer,
            Template template, String deliveryMethodId, String pubId, User user, SubscriptionDefinition subDef)
            throws InvalidSubscriptionException {
        SubscriptionRepresentation subscription = new SubscriptionRepresentation(subId, label,
                desc);
        subscription.setConsumer(consumer);
        subscription.setTemplateId(template.getId());
        subscription.setDeliveryMethodId(deliveryMethodId);
        subscription.setPublicationId(pubId);
        subscription.setUser(user);
        subscription.setParameters(resolveAndCreateParameters(subDef.getParameters(),
                template.getId()));
        subscription.setEnabled(subDef.isEnabled());
        subscription.setEndOfLife(subDef.getEndOfLife());
        return subscription;
    }


    private List<ParameterValue> resolveAndCreateParameters(List<Map<String, Object>> parameters, String templateId)
            throws InvalidSubscriptionException {
        Template template;
        try {
            template = this.templatesDao.getTemplate(templateId);
        } catch (UnknownTemplateException ex) {
            throw new InvalidSubscriptionException("Template not available: "+ templateId, ex);
        }

        final Map<String, Parameter> templateParameters = template.getParameters();

        try {
            return parameters.stream().map((Map<String, Object> t) -> {
                for (String key : t.keySet()) {
                    Parameter templateParameter = resolveTemplateParameter(templateParameters, key);
                    return new ParameterValue(key, t.get(key), templateParameter.getType());
                }

                throw new RuntimeException(new InvalidSubscriptionException("No parameter values available"));
            }).collect(Collectors.toCollection(ArrayList::new));
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof InvalidSubscriptionException) {
                throw (InvalidSubscriptionException) e.getCause();
            }
            throw new InvalidSubscriptionException("Could not resolve parameters", e);
        }
    }

    private Parameter resolveTemplateParameter(Map<String, Parameter> templateParameters, String key) {
        Optional<Parameter> match = templateParameters.keySet().stream().filter((String p) -> {
            return p.equals(key);
        }).findFirst().map((String t) -> templateParameters.get(t));

        if (match.isPresent()) {
            return match.get();
        }

        throw new RuntimeException(new InvalidSubscriptionException("Invalid template parameter: "+key));
    }

    @Override
    public void updateSubscription(SubscriptionUpdateDefinition subDef) throws InvalidSubscriptionException {
        String eolString = subDef.getEndOfLife();
        if (eolString != null && !eolString.isEmpty()) {
            DateTime eol = parseEndOfLife(eolString);
            try {
                this.dao.updateEndOfLife(subDef.getId(), eol);
            } catch (UnknownSubscriptionException ex) {
                throw new InvalidSubscriptionException(ex.getMessage(), ex);
            }

            changeEndOfLife(subDef.getId(), eol);
        }

        Boolean enabled = subDef.getEnabled();
        try {
            this.dao.updateStatus(subDef.getId(), enabled);
        } catch (UnknownSubscriptionException ex) {
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }

        if (enabled) {
            resume(subDef.getId());
        }
        else {
            pause(subDef.getId());
        }
    }

    private DateTime parseEndOfLife(String eolString) throws InvalidSubscriptionException {
        try {
            return new DateTime(eolString);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidSubscriptionException("Not a valid xs:date: "+eolString);
        }
    }

    @Override
    public void removeSubscription(String id) throws InvalidSubscriptionException {
        if (this.dao.hasSubscription(id)) {
            try {
                this.dao.remove(id);
            } catch (UnknownSubscriptionException ex) {
                throw new InvalidSubscriptionException(ex.getMessage(), ex);
            }
            remove(id);
        }
        else {
            throw new InvalidSubscriptionException("Unknown subscription: "+id);
        }
    }

    private void resume(String id) {
        LOG.debug("TODO: Implement resume");
    }

    private void pause(String id) {
        LOG.debug("TODO: Implement pause");
    }

    private void changeEndOfLife(String id, DateTime eol) {
        LOG.debug("TODO: Implement end of life update");
    }

    private void remove(String id) {
        Subscription sub;
        synchronized (this) {
            sub = this.subscriptionToRuleMap.get(id);
        }

        try {
            this.engine.removeSubscription(sub.getId());
        } catch (org.n52.subverse.subscription.UnknownSubscriptionException ex) {
            LOG.warn("Could not remove subscription", ex);
        }
    }

    private void throwExceptionOnNullOrEmpty(String value, String key) throws InvalidSubscriptionException {
        if (value == null || value.isEmpty()) {
            throw new InvalidSubscriptionException(String.format("Parameter %s cannot be null or empty", key));
        }
    }

    private Subscription wrapToSubverseSubscription(SubscriptionRepresentation subscription,
            String filterInstance, String pubId) throws InvalidSubscriptionException {
        try {
            XmlObject filterXml = XmlObject.Factory.parse(filterInstance);
            Subscription result = new Subscription(
                    subscription.getId(), new SubscribeOptions(pubId,
                            null,
                            filterXml,
                            null,
                            null,
                            Collections.emptyMap(),
                            null), null);
            return result;
        } catch (XmlException ex) {
            throw new InvalidSubscriptionException("Currently only valid XML filter definitions allowed", ex);
        }
    }

}
