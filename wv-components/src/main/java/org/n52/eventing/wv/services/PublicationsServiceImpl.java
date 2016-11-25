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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.n52.eventing.rest.publications.Publication;
import org.n52.eventing.rest.publications.PublicationsService;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.SeriesDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSeriesDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PublicationsServiceImpl implements PublicationsService {

    private static final Logger LOG = LoggerFactory.getLogger(PublicationsServiceImpl.class);

    @Autowired
    private HibernateDatabaseConnection hdc;

    @Override
    public boolean hasPublication(String id) {
        Session session = hdc.createSession();
        SeriesDao dao = new HibernateSeriesDao(session);

        try {
            Optional<Series> pub = dao.retrieveById(Integer.parseInt(id));
            return pub.isPresent();
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }
        finally {
            session.close();
        }
        return false;
    }

    @Override
    public List<Publication> getPublications() {
        Session session = hdc.createSession();
        SeriesDao dao = new HibernateSeriesDao(session);

        try {
            List<Series> pubs = dao.retrieve(null);
            return pubs.stream().map((Series s) -> {
                return wrapSeries(s);
            }).collect(Collectors.toList());
        }
        catch (NumberFormatException | DatabaseException e) {
            LOG.warn(e.getMessage());
        }
        finally {
            session.close();
        }

        return Collections.emptyList();
    }

    @Override
    public Publication getPublication(String id) throws UnknownPublicationsException {
        return null;
    }

    private Publication wrapSeries(Series s) {
        String desc = String.format("Series '%s': Phenomenon '%s' of Feature '%s'",
                s.getId(),
                s.getPhenomenon().getPhenomenonId(),
                s.getFeature().getIdentifier());
        Publication pub = new Publication(Integer.toString(s.getId()), desc, desc);
        return pub;
    }

}
