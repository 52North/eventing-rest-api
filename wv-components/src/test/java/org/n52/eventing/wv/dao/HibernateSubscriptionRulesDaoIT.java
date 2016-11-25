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

import java.util.List;
import org.n52.eventing.wv.dao.hibernate.HibernateRuleDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSeriesDao;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.eventing.wv.dao.hibernate.HibernateCategoryDao;
import org.n52.eventing.wv.dao.hibernate.HibernateFeatureOfInterestDao;
import org.n52.eventing.wv.dao.hibernate.HibernatePhenomenonDao;
import org.n52.eventing.wv.dao.hibernate.HibernateProcedureDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSubscriptionDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Category;
import org.n52.eventing.wv.model.FeatureOfInterest;
import org.n52.eventing.wv.model.Phenomenon;
import org.n52.eventing.wv.model.Procedure;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.Series;
import org.n52.eventing.wv.model.Trend;
import org.n52.eventing.wv.model.WvSubscription;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateSubscriptionRulesDaoIT {

    private Session session;

    @Before
    public void setup() throws Exception {
        HibernateDatabaseConnection hdc = new HibernateDatabaseConnection();
        hdc.afterPropertiesSet();
        this.session = hdc.createSession();
    }

    @Test
    public void roundtrip() throws ImmutableException, DatabaseException  {
        HibernateSubscriptionDao subDao = new HibernateSubscriptionDao(session);
        HibernateSeriesDao seriesDao = new HibernateSeriesDao(session);
        HibernateRuleDao ruleDao = new HibernateRuleDao(session);

        Transaction trans = session.beginTransaction();
        Series s1 = new Series();
        s1.setCategory(createCategory("test-category"));
        s1.setPhenomenon(createPhenomenon("test-phenomenon"));
        s1.setProcedure(createProcedure("test-procedure"));

        FeatureOfInterest f = new FeatureOfInterest("test-feature-"+UUID.randomUUID().toString().substring(0, 6),
                "Test Feature", "point", new Random().nextInt(100000), "its not a bug");
        new HibernateFeatureOfInterestDao(session).store(f);

        s1.setFeature(f);
        seriesDao.store(s1);

        Rule r1 = new Rule(22.0, new Trend(0, "test-trend"), 1, s1);
        ruleDao.store(r1);
        WvSubscription sub1 = new WvSubscription(r1);
        subDao.store(sub1);
        trans.commit();

        Optional<Rule> r1r = ruleDao.retrieveById(r1.getId());
        Assert.assertThat(r1r.isPresent(), CoreMatchers.is(true));
        Assert.assertThat(r1r.get().getThreshold(), CoreMatchers.is(22.0));

        Optional<WvSubscription> sub1r = subDao.retrieveById(sub1.getId());
        Assert.assertThat(sub1r.isPresent(), CoreMatchers.is(true));
        Assert.assertThat(sub1r.get().getRule().getId(), CoreMatchers.is(r1r.get().getId()));

        List<WvSubscription> subs = subDao.retrieve(null);
        Assert.assertThat(subs.size() > 0, CoreMatchers.is(true));
    }

    private Category createCategory(String name) {
        HibernateCategoryDao dao = new HibernateCategoryDao(session);
        if (dao.exists(name)) {
            return dao.retrieveByName(name).get();
        }

        return new Category(name);
    }

    private Phenomenon createPhenomenon(String name) {
        HibernatePhenomenonDao dao = new HibernatePhenomenonDao(session);
        if (dao.exists(name)) {
            return dao.retrieveByName(name).get();
        }

        return new Phenomenon(name);
    }

    private Procedure createProcedure(String name) {
        HibernateProcedureDao dao = new HibernateProcedureDao(session);
        if (dao.exists(name)) {
            return dao.retrieveByName(name).get();
        }

        return new Procedure(name);
    }

}
