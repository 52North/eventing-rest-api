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

package org.n52.eventing.rest.publications;

import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.n52.epos.event.MapEposEvent;
import org.n52.eventing.rest.Configuration;
import org.n52.subverse.engine.FilterEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummyPublicationsDao implements PublicationsService, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(DummyPublicationsDao.class);

    private final Map<String, Publication> publications = new HashMap<>();

    @Autowired
    private Configuration config;

    @Autowired
    private FilterEngine engine;

    private boolean running = true;

    public DummyPublicationsDao() {
        publications.put("dummy-pub", new Publication("dummy-pub", "dummy-pub ftw", "this publication provides niiiice data"));
    }


    @Override
    public synchronized boolean hasPublication(String id) {
        return publications.containsKey(id);
    }

    @Override
    public synchronized List<Publication> getPublications() {
        return Collections.unmodifiableList(new ArrayList<>(publications.values()));
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
        Optional<Boolean> dummy = config.getParameterAsBoolean("publishDummyMessages");
        if (dummy.isPresent() && dummy.get()) {
            new Thread(() -> {
                LOG.info("Dummy publication thread started...");

                int count = 0;
                while (running) {
                    count++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        LOG.warn(ex.getMessage(), ex);
                    }

                    long now = System.currentTimeMillis();
                    MapEposEvent e = new MapEposEvent(now, now);
                    e.put("observedProperty", "Wasserstand");
                    e.put("sensorID", "Wasserstand_Opladen");
                    e.put(MapEposEvent.GEOMETRY_KEY, new Polygon(null, null, null));
                    double val = count % 10 == 0 ? 0.3 : 0.7;
                    e.put(MapEposEvent.DOUBLE_VALUE_KEY, val);
                    e.put(MapEposEvent.ORIGNIAL_OBJECT_KEY, "{\"Wasserstand\": "+ val +"}");
                    engine.filterMessage(e, "dummy-pub");
                }
            });
        }
    }

    @Override
    public void destroy() {
        this.running = false;
    }

}
