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
package org.n52.eventing.connector.mqtt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.n52.eventing.rest.Configuration;
import org.n52.eventing.rest.publications.PublicationDataIngestor;
import org.n52.eventing.rest.publications.PublicationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PahoMqttConnector implements PublicationProvider, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(PahoMqttConnector.class);
    private String host;
    private String clientId;
    private MqttClient client;
    private MessageCallback callback;
    private boolean connected;

    @Inject
    private Optional<PublicationDataIngestor> dataIngestorOptional;

    @Autowired
    private Configuration config;

    private PublicationDataIngestor dataIngestor;
    private int port;
    private String protocol;
    private boolean parseToString;
    private String mimeType;
    private String publicationIdentifer;

    /**
     * the MQTT QoS as enum. use #ordinal() to get the int
     */
    public enum QualityOfService {
        AT_MOST_ONCE,
        AT_LEAST_ONCE,
        EXACTLY_ONCE
    }

    private static final Set<String> STRING_MIME_TYPES = new HashSet<>(Arrays.asList(
            new String[] {
                "text/csv",
                "application/json",
                "application/xml",
                "text/xml",
                "text/plain"
            }));

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!dataIngestorOptional.isPresent()) {
            LOG.warn("No dataIngestor available, skipping MQTT connector init");
            return;
        }
        this.dataIngestor = dataIngestorOptional.get();

        Optional<String> hostOpt = this.config.getParameter("connector.mqtt.host");
        if (!hostOpt.isPresent()) {
            LOG.warn("No connector.mqtt.host specified, skipping connector init");
            return;
        }

        Optional<String> publicationIdentiferOpt = this.config.getParameter("connector.mqtt.publicationIdentifer");
        if (!publicationIdentiferOpt.isPresent()) {
            LOG.warn("connector.mqtt.publicationIdentifer specified. It is required in order to relate data to the publication, skipping connector init");
            return;
        }
        this.publicationIdentifer = publicationIdentiferOpt.get();

        Integer p = this.config.getParameterAsInt("connector.mqtt.port").orElse(1883);
        String proto = this.config.getParameter("connector.mqtt.protocol").orElse("tcp");

        Optional<String> mimeOpt = this.config.getParameter("connector.mqtt.mimeType");
        if (mimeOpt.isPresent() && STRING_MIME_TYPES.contains(mimeOpt.get())) {
            this.parseToString = true;
            this.mimeType = mimeOpt.get();
        }

        init(proto,
                hostOpt.get(),
                p,
                UUID.randomUUID().toString(),
                (msg) -> {
                    if (parseToString) {
                        dataIngestor.ingestData(new String(msg), this.publicationIdentifer, this.mimeType);
                    }
                    else {
                        dataIngestor.ingestData(msg, this.publicationIdentifer);
                    }
                });

        try {
            connect();
            subscribe(config.getParameter("connector.mqtt.topic").orElse("#"), QualityOfService.EXACTLY_ONCE);
        }
        catch (MqttException e) {
            LOG.warn("Could not connect to MQTT host: {}", e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
    }

    /**
     * @param host the IP or DNS name of the broker
     * @param clientId a client id
     * @param cb the callback for reception of messages
     */
    protected void init(String protocol, String host, int port, String clientId, MessageCallback cb) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.callback = cb;
    }

    /**
     * connects the client
     *
     * @throws MqttException
     */
    public void connect() throws MqttException {
        this.client = new MqttClient(String.format("%s://%s:%s", protocol, host, port), clientId,
                new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(5000);
        options.setAutomaticReconnect(true);

        LOG.info("Connecting to {} with client-id {}", String.format("%s://%s:%s", protocol, host, port), clientId);
        client.connect(options);

        client.setCallback(new MqttCallbackExtended () {
            @Override
            public void connectionLost(Throwable cause) {
                LOG.warn("Connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                LOG.trace("New message on topic '{}': {}", topic, message);
                try {
                    callback.receive(message.getPayload());
                }
                catch (RuntimeException e) {
                    LOG.warn("Error in callback: {}", e.getMessage());
                    LOG.debug(e.getMessage(), e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                LOG.info("Delivery completed for message id '{}'", token.getMessageId());
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                LOG.info("Re-connected to MQTT broker.");
            }

        });

        this.connected = true;
    }

    /**
     * subscribe for a topic
     *
     * @param topic the topic to subscribe to
     * @param qos the QoS level
     * @throws MqttException if something goes wrong
     */
    public void subscribe(String topic, QualityOfService qos) throws MqttException {
        client.subscribe(topic, qos.ordinal());
        LOG.info("Subscribed to topic {}", topic);
    }

    @Override
    public void destroy() {
        try {
            if (this.client.isConnected() && this.connected) {
                this.client.disconnectForcibly(5000);
            }
        } catch (MqttException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    @Override
    public String getContentType() {
        return this.mimeType != null ? this.mimeType : null;
    }

    @Override
    public String getDescription() {
        return "MQTT Data feed: "+this.publicationIdentifer;
    }

    @Override
    public String getIdentifier() {
        return publicationIdentifer;
    }

    public static interface MessageCallback {

        void receive(byte[] msg);

    }

}
