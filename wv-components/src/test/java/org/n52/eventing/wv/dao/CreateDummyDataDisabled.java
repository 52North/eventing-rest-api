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
import java.util.Random;
import java.util.UUID;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.n52.eventing.wv.dao.hibernate.HibernateCategoryDao;
import org.n52.eventing.wv.dao.hibernate.HibernateEventDao;
import org.n52.eventing.wv.dao.hibernate.HibernateFeatureOfInterestDao;
import org.n52.eventing.wv.dao.hibernate.HibernateGroupDao;
import org.n52.eventing.wv.dao.hibernate.HibernatePhenomenonDao;
import org.n52.eventing.wv.dao.hibernate.HibernateProcedureDao;
import org.n52.eventing.wv.dao.hibernate.HibernateRuleDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSeriesDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSubscriptionDao;
import org.n52.eventing.wv.dao.hibernate.HibernateTrendDao;
import org.n52.eventing.wv.dao.hibernate.HibernateUnitDao;
import org.n52.eventing.wv.dao.hibernate.HibernateUserDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Category;
import org.n52.eventing.wv.model.FeatureOfInterest;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.Phenomenon;
import org.n52.eventing.wv.model.Procedure;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.Series;
import org.n52.eventing.wv.model.Unit;
import org.n52.eventing.wv.model.WvEvent;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;
import org.n52.eventing.wv.security.GroupPolicies;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class CreateDummyDataDisabled {

    private final HibernateDatabaseConnection hdc;
    private final Session session;
    private final BCryptPasswordEncoder encoder;
    private GroupPolicies gp = new GroupPolicies();

    public CreateDummyDataDisabled() throws Exception {
        this.hdc = new HibernateDatabaseConnection();
        this.hdc.afterPropertiesSet();
        this.session = this.hdc.createSession();
        this.encoder = new BCryptPasswordEncoder();
    }

    protected void shutdown() {
        this.session.close();
    }

    public static void main(String[] args) throws Exception {
        CreateDummyDataDisabled dummy = new CreateDummyDataDisabled();
        dummy.feed();
        dummy.shutdown();
    }

    private void feed() throws DatabaseException {
        HibernateGroupDao groupDao = new HibernateGroupDao(session, gp);
        HibernateUserDao userDao = new HibernateUserDao(session);
        HibernateSubscriptionDao subDao = new HibernateSubscriptionDao(session);
        HibernateSeriesDao seriesDao = new HibernateSeriesDao(session);
        HibernateRuleDao ruleDao = new HibernateRuleDao(session);
        HibernateTrendDao trendDao = new HibernateTrendDao(session);
        HibernateEventDao eventDao = new HibernateEventDao(session);

        Transaction trans = session.beginTransaction();
        Group adminGroup = new Group("admins", "admin group", true);
        groupDao.store(adminGroup);

        WvUser u = new WvUser("matthes", this.encoder.encode("asdf"), "m", "r", "m.rieke@52north.org", 1,
                Collections.singleton(adminGroup));
        userDao.store(u);

        for (int j = 0; j < 2; j++) {
            WvSubscription subscription = createNewSubscription(seriesDao, ruleDao, trendDao);
            if (j % 2 == 0) {
                subscription.setUser(u);
            }
            else {
                subscription.setGroup(adminGroup);
            }
            subDao.store(subscription);

            MutableDateTime increasingDate = new MutableDateTime();
            increasingDate.addMonths(new Random().nextInt(5)*-1);
            double startValue = new Random().nextDouble()*180;
            for (int i = 0; i < 60; i++) {
                DateTime start = increasingDate.toDateTime();
                increasingDate.addHours(12);
                DateTime end = increasingDate.toDateTime();
                WvEvent e = new WvEvent(subscription.getRule(),
                        start.toDate(),
                        startValue+i,
                        end.toDate(),
                        startValue+i-1);
                eventDao.store(e);
            }
        }

        trans.commit();
    }

   private WvSubscription createNewSubscription(HibernateSeriesDao seriesDao, HibernateRuleDao ruleDao, HibernateTrendDao trendDao) throws DatabaseException {
        HibernateUnitDao unitDao = new HibernateUnitDao(session);
        Unit u1 = new Unit("cm");
        unitDao.store(u1);

        Series s1 = new Series();
        s1.setUnit(u1);
        s1.setCategory(createCategory("test-category"));
        s1.setPhenomenon(createPhenomenon("test-phenomenon"));
        s1.setProcedure(createProcedure("test-procedure"));

        FeatureOfInterest f = new FeatureOfInterest("test-feature-"+UUID.randomUUID().toString().substring(0, 6),
                "Test Feature", "point", new Random().nextInt(100000), "its not a bug");
        new HibernateFeatureOfInterestDao(session).store(f);

        s1.setFeature(f);
        seriesDao.store(s1);

        Rule r1 = new Rule(22.0, trendDao.retrieveByDomainTrend(TrendDao.DomainTrend.LessLess).get(), 1, s1);
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

}
