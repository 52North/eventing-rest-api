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
import java.util.Optional;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvUser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateUserGroupsDao implements UserGroupsDao {

    private HibernateDatabaseConnection connection;

    @Override
    public Optional<WvUser> retrieveUserById(int id) {
        try (Session session = connection.createSession()) {
            Transaction t = session.beginTransaction();
            WvUser retrieved = session.get(WvUser.class, id);
            retrieved = initializeProxies(retrieved);
            t.commit();
            return Optional.ofNullable(retrieved);
        }
    }

    @Override
    public Optional<Group> retrieveGroupByName(String name) throws DatabaseException {
        try (Session session = connection.createSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Group> query = builder.createQuery(Group.class);
            Root<Group> root = query.from(Group.class);
            query.where(builder.equal(root.get("name"), name));
            List<Group> result = session.createQuery(query).list();
            return Optional.ofNullable(result.isEmpty() ? null : result.get(0));
        }
    }

    @Override
    public Optional<WvUser> retrieveUserByName(String name) throws DatabaseException {
        try (Session session = connection.createSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<WvUser> query = builder.createQuery(WvUser.class);
            Root<WvUser> root = query.from(WvUser.class);
            query.where(builder.equal(root.get("name"), name));
            List<WvUser> result = session.createQuery(query).list();
            WvUser user = initializeProxies(result.isEmpty() ? null : result.get(0));
            return Optional.ofNullable(user);
        }
    }




    @Override
    public Optional<Group> retrieveGroupById(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<WvUser> retrieveAllUsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Group> retrieveAllGroups() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void storeUser(WvUser u) throws ImmutableException, DatabaseException {
        internalPersist(u);
    }

    @Override
    public void storeGroup(Group g) throws ImmutableException, DatabaseException {
        internalPersist(g);
    }

    @Autowired
    public void setConnection(HibernateDatabaseConnection hdc) {
        this.connection = hdc;
    }


    private void internalPersist(Object o) throws DatabaseException {
        try (Session session = connection.createSession()) {
            Transaction t = session.beginTransaction();

            session.persist(o);

            try {
                t.commit();
            }
            catch (PersistenceException e) {
                throw new DatabaseException("Could not store object", e);
            }
        }
    }

    private WvUser initializeProxies(WvUser u) {
        if (u != null) {
            if (u.getGroups()!= null) {
                Hibernate.initialize(u.getGroups());
            }
        }
        return u;
    }

}
