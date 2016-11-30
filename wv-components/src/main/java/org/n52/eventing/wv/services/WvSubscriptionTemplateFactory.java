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

package org.n52.eventing.wv.services;

import java.util.HashMap;
import java.util.Map;
import org.n52.eventing.rest.parameters.ParameterDefinition;
import org.n52.eventing.rest.templates.Definition;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.eventing.wv.model.Rule;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class WvSubscriptionTemplateFactory {

    public static final String USER_PARAMETER = "userId";
    public static final String GROUP_PARAMETER = "groupId";

    public TemplateDefinition createTemplate(Rule r) {
        String label = String.format("Rule '%s' for Series '%s'", r.getId(), r.getSeries().getId());
        String desc = String.format("Rule '%s' for Series '%s': '%s' of feature '%s' using procedure '%s'",
                r.getId(),
                r.getSeries().getId(),
                r.getSeries().getPhenomenon().getPhenomenonId(),
                r.getSeries().getFeature().getIdentifier(),
                r.getSeries().getProcedure().getProcedureId());
        TemplateDefinition result = new TemplateDefinition(Integer.toString(r.getId()), label, desc, null);
        Map<String, Object> props = new HashMap<>();
        props.put("trend", r.getTrendCode().toString());
        props.put("threshold", r.getThreshold());
        props.put("feature", r.getSeries().getFeature().getIdentifier());
        props.put("category", r.getSeries().getCategory().getCategoryId());
        props.put("phenomenon", r.getSeries().getPhenomenon().getPhenomenonId());
        props.put("procedure", r.getSeries().getProcedure().getProcedureId());
        result.setDefinition(new Definition(props, null));

        result.addParameter(USER_PARAMETER, new ParameterDefinition("number", "The User reference id", true));
        result.addParameter(GROUP_PARAMETER, new ParameterDefinition("number", "The Group reference id", true));

        return result;
    }

}
