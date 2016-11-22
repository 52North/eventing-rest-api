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
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.User;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateUserGroupsDaoIT {

    private HibernateUserGroupsDao userDao;

    @Before
    public void setup() throws Exception {
        HibernateDatabaseConnection hdc = new HibernateDatabaseConnection();
        hdc.afterPropertiesSet();

        this.userDao = new HibernateUserGroupsDao();
        this.userDao.setConnection(hdc);
    }

    @Test
    public void roundtrip() throws ImmutableException, DatabaseException  {
        Group g = new Group("publisher", "Publishing users", true);
        this.userDao.addGroup(g);

        for (int i = 0; i < 3; i++) {
            User e1 = new User();
            e1.setName(UUID.randomUUID().toString().substring(0, 15));
            e1.setPassword("asdf");
            e1.setFirstName("peter"+i);
            e1.setLastName("chen");
            e1.setGroups(Collections.singleton(g));

            this.userDao.addUser(e1);

            Optional<User> r1 = this.userDao.retrieveUserById(e1.getId());
            Assert.assertThat(r1.get().getName(), CoreMatchers.equalTo(e1.getName()));
            Assert.assertThat(r1.get().getGroups(), CoreMatchers.hasItem(g));
        }
    }


}
