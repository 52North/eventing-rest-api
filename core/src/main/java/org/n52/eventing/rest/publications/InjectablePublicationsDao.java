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
package org.n52.eventing.rest.publications;

import org.n52.eventing.rest.model.Publication;
import org.n52.eventing.rest.model.impl.PublicationImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.eventing.rest.Configuration;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class InjectablePublicationsDao implements PublicationsService, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(InjectablePublicationsDao.class);

    private final Map<String, Publication> publications = new HashMap<>();

    @Autowired
    private Configuration config;

    @Autowired
    private List<PublicationProvider> publicationProviders;

    public InjectablePublicationsDao() {
    }


    @Override
    public synchronized boolean hasPublication(String id) {
        return publications.containsKey(id);
    }

    @Override
    public synchronized QueryResult<Publication> getPublications(Pagination p) {
        List<Publication> data = Collections.unmodifiableList(new ArrayList<>(publications.values()));
        return new QueryResult<>(data, data.size());
    }

    @Override
    public synchronized Publication getPublication(String id) throws UnknownPublicationsException {
        if (!hasPublication(id)) {
            throw new UnknownPublicationsException("Publication does not exist: "+id);
        }

        return publications.get(id);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.publicationProviders.stream()
                .forEach(pp -> {
                    if (this.publications.containsKey(pp.getIdentifier())) {
                        LOG.warn("Duplicate publication id: "+pp.getIdentifier());
                        return;
                    }

                    Publication pub = new PublicationImpl(pp.getIdentifier(), pp.getDescription(), null);
                    this.publications.put(pp.getIdentifier(), pub);
                });
    }

    @Override
    public void destroy() {
    }

}
