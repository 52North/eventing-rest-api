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

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.eventing.wv.dao.hibernate.HibernateCategoryDao;
import org.n52.eventing.wv.dao.hibernate.HibernateEventDao;
import org.n52.eventing.wv.dao.hibernate.HibernateFeatureOfInterestDao;
import org.n52.eventing.wv.dao.hibernate.HibernatePhenomenonDao;
import org.n52.eventing.wv.dao.hibernate.HibernateProcedureDao;
import org.n52.eventing.wv.dao.hibernate.HibernateRuleDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSeriesDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSubscriptionDao;
import org.n52.eventing.wv.dao.hibernate.HibernateTrendDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Category;
import org.n52.eventing.wv.model.FeatureOfInterest;
import org.n52.eventing.wv.model.Phenomenon;
import org.n52.eventing.wv.model.Procedure;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.Series;
import org.n52.eventing.wv.model.Trend;
import org.n52.eventing.wv.model.WvEvent;
import org.n52.eventing.wv.model.WvSubscription;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateEventDaoIT {

    private HibernateDatabaseConnection hdc;
    private Session session;

    @Before
    public void setup() throws Exception {
        this.hdc = new HibernateDatabaseConnection();
        this.hdc.afterPropertiesSet();
        this.session = this.hdc.createSession();
    }

    @Test
    public void testRetrievalBySubscription() throws DatabaseException {
        HibernateEventDao dao = new HibernateEventDao(session);
        HibernateSubscriptionDao subDao = new HibernateSubscriptionDao(session);
        HibernateSeriesDao seriesDao = new HibernateSeriesDao(session);
        HibernateRuleDao ruleDao = new HibernateRuleDao(session);

        Transaction trans = session.beginTransaction();

        WvSubscription sub1 = createNewSubscription(seriesDao, ruleDao, 67);
        subDao.store(sub1);

        WvSubscription sub2 = createNewSubscription(seriesDao, ruleDao, 68);
        subDao.store(sub2);

        WvEvent ev1 = new WvEvent(sub1.getRule(), new Date(), 5.0, new Date(), 4.0);
        WvEvent ev2 = new WvEvent(sub2.getRule(), new Date(), 3.0, new Date(), 2.0);
        dao.store(ev1);
        dao.store(ev2);

        trans.commit();

        List<WvEvent> sub1events = dao.retrieveForSubscription(sub1.getId());
        List<WvEvent> sub2events = dao.retrieveForSubscription(sub2.getId());

        Assert.assertThat(sub1events.size(), CoreMatchers.is(1));
        Assert.assertThat(sub2events.size(), CoreMatchers.is(1));

        Assert.assertThat(sub1events.get(0).getValue(), CoreMatchers.is(5.0));
        Assert.assertThat(sub1events.get(0).getPreviousValue(), CoreMatchers.is(4.0));

    }

    private WvSubscription createNewSubscription(HibernateSeriesDao seriesDao, HibernateRuleDao ruleDao, int trendCode) throws DatabaseException {
        Series s1 = new Series();
        s1.setCategory(createCategory("test-category"));
        s1.setPhenomenon(createPhenomenon("test-phenomenon"));
        s1.setProcedure(createProcedure("test-procedure"));

        FeatureOfInterest f = new FeatureOfInterest("test-feature-"+UUID.randomUUID().toString().substring(0, 6),
                "Test Feature", "point", new Random().nextInt(100000), "its not a bug");
        new HibernateFeatureOfInterestDao(session).store(f);

        s1.setFeature(f);
        seriesDao.store(s1);

        Rule r1 = new Rule(22.0, new HibernateTrendDao(session).retrieveByDomainTrend(TrendDao.DomainTrend.LessEqual).get(), 1, s1);
        ruleDao.store(r1);

        WvSubscription sub1 = new WvSubscription(r1);
        return sub1;
    }

    private Category createCategory(String name) throws DatabaseException {
        HibernateCategoryDao dao = new HibernateCategoryDao(session);
        if (dao.exists(name)) {
            return dao.retrieveByName(name).get();
        }
        Category r = new Category(name);
        dao.store(r);

        return r;
    }

    private Phenomenon createPhenomenon(String name) throws DatabaseException {
        HibernatePhenomenonDao dao = new HibernatePhenomenonDao(session);
        if (dao.exists(name)) {
            return dao.retrieveByName(name).get();
        }
        Phenomenon r = new Phenomenon(name);
        dao.store(r);

        return r;
    }

    private Procedure createProcedure(String name) throws DatabaseException {
        HibernateProcedureDao dao = new HibernateProcedureDao(session);
        if (dao.exists(name)) {
            return dao.retrieveByName(name).get();
        }
        Procedure r = new Procedure(name);
        dao.store(r);

        return r;
    }

    @After
    public void shutdown() {
        this.session.close();
    }

}
