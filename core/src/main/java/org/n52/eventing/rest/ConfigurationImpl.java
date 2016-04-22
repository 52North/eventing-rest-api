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

package org.n52.eventing.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConfigurationImpl implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationImpl.class);
    private final static String CONFIG_FILE = "/config.json";
    private final static String CONFIG_DEFAULT_FILE = "/config-default.json";
    private final Map<String, JsonNode> config;

    public ConfigurationImpl() {
        this(CONFIG_FILE);
    }

    public ConfigurationImpl(String configFileResource) {
        this.config = readDefaultConfig(configFileResource);
    }

    private Map<String, JsonNode> readDefaultConfig(String configFileResource) {
        try {
            InputStream configResource = getClass().getResourceAsStream(configFileResource);
            if (configResource == null) {
                configResource = getClass().getResourceAsStream(CONFIG_DEFAULT_FILE);
            }
            Map<String, JsonNode> result = new ObjectMapper().readValue(configResource, TypeFactory
                    .defaultInstance()
                    .constructMapLikeType(HashMap.class, String.class, JsonNode.class));
            configResource.close();
            return result;
        }
        catch (IOException e) {
            LOG.error("Could not load files {}, {}. Using empty config.", configFileResource,
                    CONFIG_DEFAULT_FILE, e);
            return new HashMap<>();
        }
    }

    @Override
    public Optional<String> getParameter(String key) {
        JsonNode value = this.config.get(key);
        if (value != null && value.isTextual()) {
            return Optional.of(value.asText());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getParameterAsInt(String key) {
        JsonNode value = this.config.get(key);
        if (value != null && value.isInt()) {
            return Optional.of(value.asInt());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> getParameterAsDouble(String key) {
        JsonNode value = this.config.get(key);
        if (value != null && value.isDouble()) {
            return Optional.of(value.asDouble());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getParameterAsBoolean(String key) {
        JsonNode value = this.config.get(key);
        if (value != null && value.isBoolean()) {
            return Optional.of(value.asBoolean());
        }
        return Optional.empty();
    }

}
