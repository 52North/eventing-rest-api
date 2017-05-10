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

package org.n52.eventing.delivery.mqtt;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PahoMqttDelivery {

    private static final Logger LOG = LoggerFactory.getLogger(PahoMqttDelivery.class);

    private final String protocol;
    private final String host;
    private final int port;
    private final String clientId;

    private MqttClient client;
    private boolean destroyed;

    public PahoMqttDelivery(String host, int port, String protocol, String clientId) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.clientId = clientId;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                LOG.trace("Delivery completed for message id '{}'", token.getMessageId());
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                LOG.info("Re-connected to MQTT broker.");
            }

        });

    }


    public void destroy() {
        try {
            if (this.client.isConnected()) {
                this.client.disconnectForcibly(5000);
            }
        } catch (MqttException ex) {
            LOG.warn(ex.getMessage(), ex);
        }

        this.destroyed = true;
    }

    public void publishToTopic(InputStream asStream, String topic) {
        if (this.destroyed) {
            return;
        }

        if (this.client == null || !this.client.isConnected()) {
            try {
                connect();
            } catch (MqttException ex) {
                LOG.warn("Could not connect to MQTT host", ex);
                LOG.debug(ex.getMessage(), ex);
                return;
            }
        }

        try {
            LOG.trace("Publishing message");
            this.client.publish(topic, new MqttMessage(ByteStreams.toByteArray(asStream)));
            LOG.trace("Message published");
        } catch (IOException | MqttException ex) {
            LOG.warn("Could publish message", ex);
            LOG.debug(ex.getMessage(), ex);
        }
    }

}
