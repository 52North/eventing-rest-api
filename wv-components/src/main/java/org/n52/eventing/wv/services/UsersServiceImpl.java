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

import java.util.Optional;
import org.hibernate.Session;
import org.n52.eventing.rest.users.UnknownUserException;
import org.n52.eventing.rest.users.User;
import org.n52.eventing.rest.users.UsersService;
import org.n52.eventing.wv.model.UserWrapper;
import org.n52.eventing.wv.model.WvUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.n52.eventing.wv.dao.UserDao;
import org.n52.eventing.wv.dao.hibernate.HibernateUserDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.security.GroupPolicies;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UsersServiceImpl implements UsersService {

    private static final Logger LOG = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    private HibernateDatabaseConnection hdc;
    
    @Autowired
    private GroupPolicies groupPolicies;

    @Override
    public User getUser(String id) throws UnknownUserException {
        Session session = hdc.createSession();
        UserDao delegate = new HibernateUserDao(session, groupPolicies);
        try {
            Optional<WvUser> result = delegate.retrieveByName(id);
            if (result.isPresent()) {
                return new UserWrapper(result.get(), false);
            }
        }
        finally {
            session.close();
        }

        throw new UnknownUserException("Could not find user..");
    }

    @Override
    public boolean hasUser(String id) {
        Session session = hdc.createSession();
        UserDao delegate = new HibernateUserDao(session, groupPolicies);
        try {
            return delegate.retrieveByName(id).isPresent();
        }
        finally {
            session.close();
        }
    }

}
