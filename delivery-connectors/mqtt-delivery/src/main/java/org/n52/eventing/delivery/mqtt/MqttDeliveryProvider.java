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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.n52.eventing.rest.Configuration;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.DeliveryParameter;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.Streamable;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class MqttDeliveryProvider implements DeliveryProvider, InitializingBean, DisposableBean {

    @Autowired
    private Configuration config;
    private PahoMqttDelivery deliverer;

    @Override
    public String getIdentifier() {
        return "mqtt-delivery";
    }

    @Override
    public String getAbstract() {
        return "MQTT delivery via hosted broker";
    }

    @Override
    public DeliveryParameter[] getParameters() {
        return new DeliveryParameter[] {new DeliveryParameter("string", null, "topic", null)};
    }

    @Override
    public DeliveryEndpoint createDeliveryEndpoint(DeliveryDefinition def) throws UnsupportedDeliveryDefinitionException {
        Optional<String> topicOpt = def.getParameters().stream()
                .filter(t -> t.getElementName().equals("topic"))
                .findFirst()
                .map(dp -> dp.getValue());

        return new MqttDeliveryEndpoint(topicOpt.orElse(UUID.randomUUID().toString().substring(0, 8)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Optional<String> hostOpt = config.getParameter("delivery.mqtt.host");
        if (!hostOpt.isPresent()) {
            throw new IllegalArgumentException("Config parameter 'delivery.mqtt.host' is required!");
        }
        String host = hostOpt.get();

        String protocol = config.getParameter("delivery.mqtt.protocol").orElse("tcp");
        Integer port = config.getParameterAsInt("delivery.mqtt.port").orElse(1883);

        this.deliverer = new PahoMqttDelivery(host, port, protocol, UUID.randomUUID().toString());
    }

    @Override
    public void destroy() throws Exception {
        if (this.deliverer != null) {
            this.deliverer.destroy();
        }
    }

    @Override
    public Map<? extends String, ? extends String> getNamespacePrefixMap() {
        return Collections.emptyMap();
    }

    private class MqttDeliveryEndpoint implements DeliveryEndpoint {

        private final String topic;

        public MqttDeliveryEndpoint(String topic) {
            this.topic = topic;
        }

        @Override
        public void deliver(Optional<Streamable> o, boolean asRaw) {
            if (o.isPresent()) {
                deliverer.publishToTopic(o.get().asStream(), this.topic);
            }
        }

        @Override
        public String getEffectiveLocation() {
            return String.format("%s://%s:%s/%s",
                    deliverer.getProtocol(), deliverer.getHost(), deliverer.getPort(), topic);
        }

        @Override
        public void destroy() {
        }

    }

}
