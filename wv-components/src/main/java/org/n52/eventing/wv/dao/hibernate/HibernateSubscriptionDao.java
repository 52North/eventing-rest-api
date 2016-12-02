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
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;
import org.n52.eventing.wv.dao.SubscriptionDao;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateSubscriptionDao extends BaseHibernateDao<WvSubscription> implements SubscriptionDao {

    public HibernateSubscriptionDao(Session session) {
        super(session);
    }

    @Override
    public List<WvSubscription> retrieveByUser(WvUser user, Pagination pagination) throws DatabaseException {
        String param = "userId";
        String entity = WvSubscription.class.getSimpleName();
        String hql = String.format("SELECT s FROM %s s join s.user u WHERE u.id=:%s order by s.id asc", entity, param);
        Query q = getSession().createQuery(hql);

        if (pagination != null) {
            q.setFirstResult(pagination.getOffset());
            q.setMaxResults(pagination.getLimit());
        }

        q.setParameter(param, user.getId());
        return q.list();
    }

    @Override
    public List<WvSubscription> retrieveByUser(WvUser user) throws DatabaseException {
        return retrieveByUser(user, null);
    }

    @Override
    public List<WvSubscription> retrieveByGroup(Group group, Pagination pagination) throws DatabaseException {
        String param = "groupId";
        String entity = WvSubscription.class.getSimpleName();
        String hql = String.format("SELECT s FROM %s s join s.group g WHERE g.id=:%s order by s.id asc", entity, param);
        Query q = getSession().createQuery(hql);

        if (pagination != null) {
            q.setFirstResult(pagination.getOffset());
            q.setMaxResults(pagination.getLimit());
        }

        q.setParameter(param, group.getId());
        return q.list();
    }

    @Override
    public List<WvSubscription> retrieveByGroup(Group group) throws DatabaseException {
        return retrieveByGroup(group, null);
    }

    @Override
    public boolean hasEntity(WvSubscription subscription) {
        String paramGroup = "groupId";
        String paramUser = "userId";
        String paramRule = "ruleId";
        String entity = WvSubscription.class.getSimpleName();
        String hql = String.format("SELECT s FROM %s s join s.rule r WHERE r.id=:%s ", entity, paramRule);
        if (subscription.getGroup() != null) {
            hql = String.format("%s AND s.group.id=:%s", hql, paramGroup);
        }
        if (subscription.getUser()!= null) {
            hql = String.format("%s AND s.user.id=:%s", hql, paramUser);
        }

        Query q = getSession().createQuery(hql);
        q.setParameter(paramRule, subscription.getRule().getId());

        if (subscription.getGroup() != null) {
            q.setParameter(paramGroup, subscription.getGroup().getId());
        }
        if (subscription.getUser()!= null) {
            q.setParameter(paramUser, subscription.getUser().getId());
        }

        return q.list().size() > 0;
    }

}
