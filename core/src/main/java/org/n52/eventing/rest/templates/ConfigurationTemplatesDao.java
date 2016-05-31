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

package org.n52.eventing.rest.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.n52.eventing.rest.Configuration;
import org.n52.eventing.rest.Constructable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConfigurationTemplatesDao implements TemplatesDao, Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationTemplatesDao.class);
    private final Map<String, TemplateDefinition> templates = new ConcurrentHashMap<>();

    @Autowired
    private Configuration config;

    public ConfigurationTemplatesDao() {
    }

    @Override
    public boolean hasTemplate(String id) {
        return templates.containsKey(id);
    }

    @Override
    public TemplateDefinition getTemplate(String id) throws UnknownTemplateException {
        if (hasTemplate(id)) {
            return templates.get(id);
        }

        throw new UnknownTemplateException("not there: "+ id);
    }

    @Override
    public List<TemplateDefinition> getTemplates() {
        return Collections.unmodifiableList(new ArrayList<>(templates.values()));
    }

    @Override
    public void construct() {
        LOG.info("Templates DAO, using configuration: {}", config);
        try {
            loadTemplates(config);
        } catch (IOException ex) {
            LOG.warn("Could not load templates", ex);
        }
    }

    protected void loadTemplates(Configuration config) throws IOException {
        String baseDir = config.getParameter("templateDirectory").orElse("/templates");
        try  {
            URL res = getClass().getResource(baseDir);
            Path basePath = Paths.get(res.toURI());
            Files.find(basePath, 1, (Path t, BasicFileAttributes u) -> {
                return t.toFile().toString().endsWith(".json");
            }).forEach(p -> {
                try {
                    TemplateDefinition t = loadTemplate(p);
                    if (templates.containsKey(t.getId())) {
                        LOG.warn("Template with id '{}' already registered!", t.getId());
                    }
                    else {
                        templates.put(t.getId(), t);
                        LOG.info("Added template '{}'", t.getId());
                    }
                } catch (IOException e) {
                    LOG.warn("Could not template instance {}", p, e);
                }
            });
        }
        catch (URISyntaxException e) {
            LOG.warn("Could not resolve templates dir", e);
        }

    }

    protected TemplateDefinition loadTemplate(Path p) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TemplateDefinition t = mapper.readValue(p.toFile(), TemplateDefinition.class);
        return t;
    }

}
