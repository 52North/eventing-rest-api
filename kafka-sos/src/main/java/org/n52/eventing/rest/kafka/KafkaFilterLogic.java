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
package org.n52.eventing.rest.kafka;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodInstance;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsService;
import org.n52.eventing.rest.subscriptions.FilterLogic;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.n52.eventing.rest.subscriptions.SubscriptionInstance;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.streamable.StringStreamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class KafkaFilterLogic implements FilterLogic {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFilterLogic.class.getName());
    private static final String APPLICATION_JSON = "application/json";

    @Autowired
    private KafkaSosConsumerWrapper wrapper;


    @Autowired
    private DeliveryMethodsService deliveryMethodsDao;

    private final Map<String, SubscriptionInstance> subscriptions = new HashMap<>();
    private final Map<String, StoppableConsumer> consumers = new HashMap<>();



    @Override
    public String internalSubscribe(SubscriptionInstance subscription, TemplateDefinition template) throws InvalidSubscriptionException {
        /*
        * resolve delivery endpoint
        */
        List<DeliveryEndpoint> endpoints = subscription.getDeliveryMethods().stream().map((DeliveryMethodInstance dm) -> {
            try {
                DeliveryEndpoint result = this.deliveryMethodsDao.createDeliveryEndpoint(dm,
                        subscription.getPublicationId());
                String effLoc = result.getEffectiveLocation();
                if (effLoc != null) {
                    dm.setDetails(Collections.singletonMap("effectiveLocation", result.getEffectiveLocation()));
                }
                return result;
            } catch (InvalidSubscriptionException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        BrokeringDeliveryEndpoint brokeringEndpoint = new BrokeringDeliveryEndpoint(endpoints);

        Properties props = new Properties();
        props.put("bootstrap.servers", this.wrapper.getConfig().getProperty("bootstrapServers"));
        props.put("group.id", UUID.randomUUID().toString());
        props.put("key.deserializer", IntegerDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        StoppableConsumer consumer = new StoppableConsumer(props, brokeringEndpoint);

        String topic = this.wrapper.getProcessor().resolveTopicForOfferingId(subscription.getPublicationId());
        if (topic == null) {
            throw new InvalidSubscriptionException("Could not resolve topic for publication id: "+subscription.getPublicationId());
        }

        consumer.subscribe(Collections.singletonList(topic));

        String result = UUID.randomUUID().toString();
        this.subscriptions.put(result, subscription);
        this.consumers.put(result, consumer);
        consumer.start();

        LOG.info("New subscription stored: "+result);
        return result;
    }

    @Override
    public void remove(String id) {
        if (subscriptions.containsKey(id)) {
            this.subscriptions.remove(id);
            StoppableConsumer cons = this.consumers.get(id);
            if (cons != null) {
                cons.stop();
                cons.wakeup();
                cons.close();
            }

            LOG.info("subscription removed: "+id);
        }
        else {
            LOG.info("subscription not found: "+id);
        }
    }

    private static class StoppableConsumer extends KafkaConsumer<Integer, String> {

        private boolean stopped = false;
        private final DeliveryEndpoint delivery;

        public StoppableConsumer(Properties properties, DeliveryEndpoint delivery) {
            super(properties);
            this.delivery = delivery;
        }

        public void start() {
            new Thread(() -> {
                while (!stopped) {
                    /*
                    * listen for updates from topics forever
                    */
                    ConsumerRecords<Integer, String> records = super.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<Integer, String> record : records) {
                        LOG.debug("new record received: "+record.value());
                        this.delivery.deliver(Optional.of(new StringStreamable(record.value(), APPLICATION_JSON)), true);
                    }
                }
            }).start();
        }

        public void stop() {
            this.stopped = true;
        }
    }


}
