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

package org.n52.eventing.wv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public abstract class JsonConfigured {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConfigured.class);
    private Map<String, JsonNode> config;

    public final void init(String configFileResource) {
        this.config = readDefaultConfig(configFileResource != null ? configFileResource : getDefaultConfigFileName());
    }

    protected Map<String, JsonNode> readDefaultConfig(String configFileResource) {
        try {
            InputStream configResource = getClass().getResourceAsStream(configFileResource);
            if (configResource == null) {
                configResource = getClass().getResourceAsStream(getDefaultConfigFileName());
            }
            Map<String, JsonNode> result = new ObjectMapper().readValue(configResource, TypeFactory
                    .defaultInstance()
                    .constructMapLikeType(HashMap.class, String.class, JsonNode.class));
            configResource.close();
            return result;
        }
        catch (IOException e) {
            LOG.error("Could not load files {}, {}. Using empty config.", configFileResource,
                    getDefaultConfigFileName(), e);
            return new HashMap<>();
        }
    }

    protected Set<String> readStringArray(String property) {
        JsonNode value = this.config.get(property);
        if (value != null && value.isArray()) {
            Set<String> result = new HashSet<>();
            value.elements().forEachRemaining(node -> {
                if (node.isTextual()) {
                    result.add(node.asText());
                }
            });

            return result;
        }
        return Collections.emptySet();
    }


    protected Set<Integer> readIntegerArray(String property) {
        JsonNode value = this.config.get(property);
        if (value != null && value.isArray()) {
            Set<Integer> result = new HashSet<>();
            value.elements().forEachRemaining(node -> {
                if (node.isInt()) {
                    result.add(node.asInt());
                }
            });

            return result;
        }
        return Collections.emptySet();
    }

    protected Optional<String> readStringProperty(String property) {
        JsonNode value = this.config.get(property);
        if (value != null && value.isTextual()) {
            return Optional.of(value.asText());
        }

        return Optional.empty();
    }

    public final Map<String, JsonNode> getConfig() {
        return config;
    }

    protected abstract String getDefaultConfigFileName();

}
