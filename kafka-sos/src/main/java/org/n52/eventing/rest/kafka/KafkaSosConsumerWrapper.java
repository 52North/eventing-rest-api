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

package org.n52.eventing.rest.kafka;

import java.util.Properties;
import java.util.UUID;
import static org.n52.eventing.rest.kafka.KafkaStreamProcessor.CONSUMER_PROPERTIES_FILE;
import org.n52.kafka.sos.KafkaSosConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class KafkaSosConsumerWrapper implements InitializingBean, DisposableBean {

    private Properties config;
    private KafkaSosConsumer processor;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.config = new Properties();
        config.load(getClass().getResourceAsStream(CONSUMER_PROPERTIES_FILE));
        this.processor = new KafkaSosConsumer(0, "sos-"+UUID.randomUUID().toString(),
                config.getProperty("bootstrapServers"),
                config.getProperty("kafkaConnectRestBaseUrl"), config);

        new Thread(this.processor).start();
    }

    public Properties getConfig() {
        return config;
    }

    public KafkaSosConsumer getProcessor() {
        return processor;
    }

    @Override
    public void destroy() throws Exception {
        if (this.processor != null) {
            this.processor.shutdown();
        }
    }



}
