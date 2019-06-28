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
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.eventing.rest.parameters.ParameterInstance;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AbstractInstanceGeneratorTest {

    @Test
    public void testTemplateProcessing() {
        FilterInstanceGenerator aig = new FilterInstanceGenerator();

        String instance = aig.generateFilterInstance(createTemplate(), createValues());

        Assert.assertThat(instance, CoreMatchers.is(
                "<greaterThan><prop>temp</prop><val>22.03</val></greaterThan>"
        ));
    }

    private TemplateDefinitionImpl createTemplate() {
        TemplateDefinitionImpl t = new TemplateDefinitionImpl();
        t.setDefinition(new Definition("<greaterThan><prop>${parama}</prop><val>${paramb}</val></greaterThan>", ""));
        return t;
    }

    private List<ParameterInstance> createValues() {
        List<ParameterInstance> result = new ArrayList<>();

        result.add(new ParameterInstance("parama", "temp", "text"));
        result.add(new ParameterInstance("paramb", 22.03, "number"));

        return result;
    }

}
