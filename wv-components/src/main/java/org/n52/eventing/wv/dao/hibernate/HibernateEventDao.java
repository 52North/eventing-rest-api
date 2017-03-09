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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.dao.EventDao;
import org.n52.eventing.wv.model.Series;
import org.n52.eventing.wv.model.WvEvent;
import org.n52.eventing.wv.model.WvSubscription;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateEventDao extends BaseHibernateDao<WvEvent> implements EventDao {

    public HibernateEventDao(Session session) {
        super(session);
    }

    @Override
    public List<WvEvent> retrieveForSubscription(int idInt) {
        return retrieveForSubscription(idInt, null);
    }

    @Override
    public List<WvEvent> retrieveForSubscription(int idInt, Pagination pagination) {
        Session s = getSession();
        String param = "subId";
        String eventEntity = WvEvent.class.getSimpleName();
        String subEntity = WvSubscription.class.getSimpleName();
        String hql = String.format("SELECT e FROM %s e join e.rule r, %s s WHERE s.id=:%s AND s.rule = r order by e.timestamp asc", eventEntity, subEntity, param);
        Query query = getSession().createQuery(hql);

        if (pagination != null) {
            query.setFirstResult(pagination.getOffset());
            query.setMaxResults(pagination.getLimit());
        }

        query.setParameter(param, idInt);
        return query.list();
    }

    @Override
    public List<WvEvent> retrieveWithFilter(Map<String, String[]> filter, Pagination pagination) {
        Map<String, Integer> paramReferenceMap = new HashMap<>();


        List<String> whereClauses = new ArrayList<>();

        String[] subscriptionIdentifier = filter.get("subscription");
        String subscriptionsWhere = null;
        if (subscriptionIdentifier != null && subscriptionIdentifier.length > 0) {
            Stream<String> subscriptionIdStream = Stream.of(subscriptionIdentifier).distinct();
            Map<String, Integer> subscriptionIdMap = subscriptionIdStream.collect(Collectors.toMap(id -> "subparam"+id, id -> Integer.parseInt(id)));

            //add the params to a map, so they can later be added to the query
            paramReferenceMap.putAll(subscriptionIdMap);
            subscriptionsWhere = "("+ subscriptionIdMap.keySet().stream()
                    .map(id -> String.format("sub.id=:%s", id))
                    .collect(Collectors.joining(" OR ")) + ")";
            whereClauses.add(subscriptionsWhere);
        }

        String[] seriesIdentifier = filter.get("publication");
        String seriesWhere = null;
        if (seriesIdentifier != null && seriesIdentifier.length > 0) {
            Stream<String> seriesIdStream = Stream.of(seriesIdentifier).distinct();
            Map<String, Integer> seriesIdMap = seriesIdStream.collect(Collectors.toMap(id -> "seriesparam"+id, id -> Integer.parseInt(id)));

            //add the params to a map, so they can later be added to the query
            paramReferenceMap.putAll(seriesIdMap);
            seriesWhere = "("+ seriesIdMap.keySet().stream()
                    .map(id -> String.format("ser.id=:%s", id))
                    .collect(Collectors.joining(" OR ")) + ")";
            whereClauses.add(seriesWhere);
        }

        String entity = WvEvent.class.getSimpleName();
        String subEntity = WvSubscription.class.getSimpleName();
        String serEntity = Series.class.getSimpleName();

        //create the where clause from joining the above critera clauses
        String whereClause = whereClauses.stream()
                .collect(Collectors.joining(" AND "));

        String hql = String.format("SELECT ev FROM %s ev join ev.rule r%s%s WHERE %s AND sub.rule = r order by ev.timestamp asc",
                entity,
                subscriptionsWhere != null ? String.format(", %s sub", subEntity) : "",
                seriesWhere != null ? String.format(", %s ser", serEntity) : "",
                whereClause);

        Query q = getSession().createQuery(hql);

        if (pagination != null) {
            q.setFirstResult(pagination.getOffset());
            q.setMaxResults(pagination.getLimit());
        }

        //add the parameters as created from the above filters
        paramReferenceMap.keySet().stream().forEach(param -> q.setParameter(param, paramReferenceMap.get(param)));

        return q.list();
    }

}
