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

import java.util.ArrayList;
import org.n52.eventing.wv.dao.hibernate.HibernateGroupDao;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.eventing.wv.dao.hibernate.HibernateUserDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvUser;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateUserGroupsDaoIT {

    private HibernateDatabaseConnection hdc;
    private Session session;

    @Before
    public void setup() throws Exception {
        this.hdc = new HibernateDatabaseConnection();
        this.hdc.afterPropertiesSet();
        this.session = this.hdc.createSession();
    }

    @Test
    public void roundtrip() throws ImmutableException, DatabaseException  {
        HibernateGroupDao groupDao = new HibernateGroupDao(session);
        HibernateUserDao userDao = new HibernateUserDao(session);
        Transaction trans = session.beginTransaction();

        Optional<Group> gopt = groupDao.retrieveGroupByName("publisher");
        Group g;
        if (!gopt.isPresent()) {
            g = new Group("publisher", "Publishing users", true);
            groupDao.store(g);
        }
        else {
            g = gopt.get();
        }

        gopt = groupDao.retrieveGroupByName("publisher");
        Assert.assertThat(gopt.isPresent(), CoreMatchers.is(true));

        List<WvUser> added = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            WvUser e1 = new WvUser();
            e1.setName(UUID.randomUUID().toString().substring(0, 15));
            e1.setPassword("asdf");
            e1.setFirstName("peter"+i);
            e1.setLastName("chen");
            e1.setGroups(Collections.singleton(g));

            userDao.store(e1);
            added.add(e1);
        }

        trans.commit();

        for (int i = 0; i < 3; i++) {
            WvUser e1 = added.get(i);
            Optional<WvUser> r1 = userDao.retrieveById(e1.getId());
            Assert.assertThat(r1.get().getName(), CoreMatchers.equalTo(e1.getName()));
            Assert.assertThat(r1.get().getGroups(), CoreMatchers.hasItem(g));

            Optional<WvUser> r2 = userDao.retrieveUserByName(e1.getName());
            Assert.assertThat(r2.get().getName(), CoreMatchers.equalTo(e1.getName()));
            Assert.assertThat(r2.get().getGroups(), CoreMatchers.hasItem(g));
        }
    }

}
