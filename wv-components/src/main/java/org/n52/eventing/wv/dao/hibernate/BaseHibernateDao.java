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

package org.n52.eventing.wv.dao.hibernate;

import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.model.BaseEntity;
import org.springframework.core.GenericTypeResolver;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 * @param <T> the model entity of this Dao inherting BaseEntity
 */
public class BaseHibernateDao<T extends BaseEntity> {

    private final Class<T> genericType;
    private final Session session;

    public BaseHibernateDao(Session session) {
        this.session = session;
        this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), BaseHibernateDao.class);
    }

    public Optional<T> retrieveById(int id) {
        T retrieved = session.get(this.genericType, id);
        return Optional.ofNullable(retrieved);
    }

    public List<T> retrieve(Pagination pagination) {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(this.genericType);
        Root<T> root = criteriaQuery.from(this.genericType);
        criteriaQuery.select(root);
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("id")));
        applyCriteria(criteriaQuery, criteriaBuilder, root);
        
        Query<T> query = session.createQuery(criteriaQuery);

        if (pagination != null) {
            query.setFirstResult(pagination.getOffset());
            query.setMaxResults(pagination.getLimit());
        }

        return query.list();
    };

    public boolean exists(int id) {
        T retrieved = session.get(this.genericType, id);
        return retrieved != null;
    }

    public boolean exists(String name) {
        return retrieveByName(name).isPresent();
    }

    public Optional<T> retrieveByName(String name) {
        return retrieveByKey("name", name);
    }

    protected Optional<T> retrieveByKey(String key, String value) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.genericType);
        Root<T> root = query.from(this.genericType);
        query.where(builder.equal(root.get(key), value));
        List<T> result = getSession().createQuery(query).list();
        return Optional.ofNullable(result.isEmpty() ? null : result.get(0));
    }

    public void store(T o) throws DatabaseException {
        session.persist(o);
    }

    public void remove(T r) throws DatabaseException {
        session.delete(r);
    }

    protected Session getSession() {
        return session;
    }

    protected Class<T> getGenericType() {
        return genericType;
    }

    protected void applyCriteria(CriteriaQuery<T> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<T> from) {
    }

}
