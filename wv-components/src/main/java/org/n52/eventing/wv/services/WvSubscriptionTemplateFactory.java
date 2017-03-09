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
import org.n52.eventing.wv.i18n.I18nProvider;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.WvTemplateDefinition;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class WvSubscriptionTemplateFactory {

    public static final String USER_PARAMETER = "userId";
    public static final String GROUP_PARAMETER = "groupId";
    private final I18nProvider i18n;

    WvSubscriptionTemplateFactory(I18nProvider i18n) {
        this.i18n = i18n;
    }

    public WvTemplateDefinition createTemplate(Rule r) {
        String trendcodeLabel = i18n.getString("trendcode."+r.getTrendCode().getId());
        String label = String.format(i18n.getString("rule.label"),
                r.getSeries().getPhenomenon().getName(),
                r.getSeries().getFeature().getIdentifier(),
                trendcodeLabel,
                r.getThreshold());
        WvTemplateDefinition result = new WvTemplateDefinition(Integer.toString(r.getId()), label, null, null);

        result.setDefinition(createDefinition(r));

        result.addParameter(USER_PARAMETER, new ParameterDefinition("number", i18n.getString("rule.parameter.userId"), true));
        result.addParameter(GROUP_PARAMETER, new ParameterDefinition("number", i18n.getString("rule.parameter.groupId"), true));

        return result;
    }
    
    public Definition createDefinition(Rule r) {
        Map<String, Object> props = new HashMap<>();
        props.put("trend", r.getTrendCode().getId());
        props.put("threshold", r.getThreshold());
        return new Definition(props, null);
    }

}
