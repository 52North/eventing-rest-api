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
package org.n52.eventing.rest.templates;

import org.n52.eventing.rest.model.impl.TemplateDefinitionImpl;
import java.util.Collection;
import org.apache.commons.lang3.StringEscapeUtils;
import org.n52.eventing.rest.parameters.ParameterInstance;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class FilterInstanceGenerator {

    public String generateFilterInstance(TemplateDefinitionImpl t, Collection<ParameterInstance> values) {
        Object o = t.getDefinition().getContent();

        String content;
        if (o instanceof String) {
            content = (String) o;
        }
        else {
            throw new IllegalArgumentException("content must be string for default impl of FilterLogic");
        }

        if (content.contains("&lt;")) {
            content = StringEscapeUtils.unescapeXml(content);
        }

        for (ParameterInstance param : values) {
            content = content.replace(String.format("${%s}", param.getName()), param.getValue().toString());
        }

        return content;
    }

}
