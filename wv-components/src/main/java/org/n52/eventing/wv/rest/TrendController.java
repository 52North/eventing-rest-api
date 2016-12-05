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

package org.n52.eventing.wv.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.hibernate.HibernateTrendDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.i18n.I18nProvider;
import org.n52.eventing.wv.model.Trend;
import org.n52.eventing.wv.model.i18n.I18nTrend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/trends",
        produces = {"application/json"})
public class TrendController {

    private static final Logger LOG = LoggerFactory.getLogger(TrendController.class);

    @Autowired
    private I18nProvider i18n;

    @Autowired
    private HibernateDatabaseConnection hdc;

    @RequestMapping("")
    public List<Trend> getTrends(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException, NotAuthenticatedException {
        try (Session session = hdc.createSession()) {
            HibernateTrendDao dao = new HibernateTrendDao(session);

            return dao.retrieve(null).stream()
                    .map((Trend t) -> {
                        try {
                            Optional<I18nTrend> lt = dao.retrieveAsLocale(i18n.getLocale(), t);
                            if (lt.isPresent()) {
                                t.setDescription(lt.get().getName());
                            }
                        } catch (DatabaseException ex) {
                            LOG.warn(ex.getMessage());
                            LOG.debug(ex.getMessage(), ex);
                        }

                        return t;
                    })
                    .collect(Collectors.toList());
        }
    }

}
