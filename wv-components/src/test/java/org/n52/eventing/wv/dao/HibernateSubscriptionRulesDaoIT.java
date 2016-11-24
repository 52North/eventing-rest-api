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

package org.n52.eventing.wv.dao;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Category;
import org.n52.eventing.wv.model.FeatureOfInterest;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.Phenomenon;
import org.n52.eventing.wv.model.Procedure;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.Series;
import org.n52.eventing.wv.model.Trend;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateSubscriptionRulesDaoIT {

    private HibernateSubscriptionRulesDao dao;
    private HibernateSeriesDao seriesDao;

    @Before
    public void setup() throws Exception {
        HibernateDatabaseConnection hdc = new HibernateDatabaseConnection();
        hdc.afterPropertiesSet();

        this.dao = new HibernateSubscriptionRulesDao();
        this.dao.setConnection(hdc);
        
        this.seriesDao = new HibernateSeriesDao();
        this.seriesDao.setConnection(hdc);
    }

    @Test
    public void roundtrip() throws ImmutableException, DatabaseException  {
        Series s1 = new Series();
        s1.setCategory(new Category("test-category"));
        s1.setPhenomenon(new Phenomenon("test-phenomenon"));
        s1.setProcedure(new Procedure("test-procedure"));
        s1.setFeature(new FeatureOfInterest("test-feature", "Test Feature", "point", 0, "its not a bug"));
        this.seriesDao.storeSeries(s1);
        
        Rule r1 = new Rule(22.0, new Trend(0, "test-trend"), 1, s1);
        this.dao.storeRule(r1);
        WvSubscription sub1 = new WvSubscription(r1);
        this.dao.storeSubscription(sub1);
        
        Optional<Rule> r1r = this.dao.retrieveRule(r1.getId());
        Optional<WvSubscription> sub1r = this.dao.retrieveSubscription(sub1.getId());
    }

}
