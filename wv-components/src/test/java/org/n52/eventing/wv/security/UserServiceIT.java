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

package org.n52.eventing.wv.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.ImmutableException;
import org.n52.eventing.wv.dao.hibernate.HibernateUserDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UserServiceIT {

    private HibernateUserDao userDao;
    private UserService userService;
    private BCryptPasswordEncoder encoder;
    private Session session;

    @Before
    public void setup() throws Exception {
        HibernateDatabaseConnection hdc = new HibernateDatabaseConnection();
        hdc.afterPropertiesSet();
        this.session = hdc.createSession();

        this.userDao = new HibernateUserDao(session);

        this.encoder = new BCryptPasswordEncoder();

        this.userService = new UserService();
        this.userService.setPasswordEncoder(this.encoder);
        this.userService.setDatabaseConnection(hdc);
    }

    @Test
    public void testPrincipals() throws DatabaseException, ImmutableException {
        Transaction trans = session.beginTransaction();
        WvUser u = new WvUser();
        String password = "asdf";
        u.setName(UUID.randomUUID().toString().substring(0, 8));
        u.setPassword(encoder.encode(password));
        u.setGroups(Collections.singleton(new Group("admin", "admin users", true)));

        if (!userDao.retrieveByName(u.getName()).isPresent()) {
            userDao.store(u);
        }

        trans.commit();

        UsernamePasswordAuthenticationToken result = this.userService.authenticate(
                new UsernamePasswordAuthenticationToken(u.getName(), password));

        Assert.assertThat(result, CoreMatchers.notNullValue());
        Assert.assertThat(result.getPrincipal(), CoreMatchers.instanceOf(UserPrinciple.class));
        Assert.assertThat(((UserPrinciple) result.getPrincipal()).getUser().getName(), CoreMatchers.equalTo(u.getName()));

        Collection<GrantedAuthority> auths = result.getAuthorities();
        auths.forEach(ga -> Assert.assertThat(ga, CoreMatchers.instanceOf(GroupPrinciple.class)));

        long adminCount = auths.stream().filter((GrantedAuthority ga) -> {
            return ((GroupPrinciple) ga).getAuthority().equals("admin");
        }).count();

        Assert.assertThat(adminCount, CoreMatchers.is(1L));
    }

}
