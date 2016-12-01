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

package org.n52.eventing.wv.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Locale;
import org.n52.eventing.wv.JsonConfigured;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class I18nProvider extends JsonConfigured {

    private final static String CONFIG_FILE = "/wv/i18n.json";
    private final static String CONFIG_DEFAULT_FILE = "/wv/i18n-default.json";
    private final Locale defaultLocale;

    public I18nProvider() {
        this(CONFIG_FILE);
    }

    public I18nProvider(String configFileResource) {
        init(configFileResource);
        this.defaultLocale = Locale.forLanguageTag(getConfig().getOrDefault("defaultLocale", new TextNode("de")).asText());
    }

    @Override
    protected String getDefaultConfigFileName() {
        return CONFIG_DEFAULT_FILE;
    }

    public String getString(String key) {
        return getString(key, this.defaultLocale);
    }

    public String getString(String key, Locale l) {
        JsonNode valueObject = getConfig().get(key);
        if (valueObject == null) {
            return String.format("Translation missing for key '%s'", key);
        }

        if (valueObject.has(l.getLanguage())) {
            return valueObject.get(l.getLanguage()).asText();
        }

        return valueObject.get(this.defaultLocale.getLanguage()).asText();
    }

}
