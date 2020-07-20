/*
 * Copyright (C) 2016-2020 52°North Initiative for Geospatial Open Source
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
import org.n52.eventing.rest.parameters.ParameterDefinition;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConfigurationTemplatesDaoTest {

    @Test
    public void testTemplateLoading() throws IOException, URISyntaxException {
        ConfigurationTemplatesDao dao = new ConfigurationTemplatesDao(null);

        TemplateDefinitionImpl t = dao.loadTemplate(Paths.get(getClass().getResource("/templates-test/overshootUndershoot.json").toURI()));

        MatcherAssert.assertThat(t.getId(), CoreMatchers.is("overshootUndershoot"));
        MatcherAssert.assertThat(t.getLabel(), CoreMatchers.is("Generic overshoot/undershoot pattern"));
        MatcherAssert.assertThat(t.getParameters().size(), CoreMatchers.is(2));
        MatcherAssert.assertThat(t.getParameters().get("observedProperty").getType(), CoreMatchers.is("text"));

        ParameterDefinition thresholdValue = t.getParameters().get("thresholdValue");
        MatcherAssert.assertThat(thresholdValue.getMin(), CoreMatchers.is(1.3));
        MatcherAssert.assertThat(thresholdValue.getMax(), CoreMatchers.is(2.2));
        MatcherAssert.assertThat(thresholdValue.getPattern(), CoreMatchers.is("regex"));
    }

}
