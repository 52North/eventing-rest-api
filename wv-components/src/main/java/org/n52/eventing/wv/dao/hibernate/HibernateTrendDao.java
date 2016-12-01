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

import java.util.Locale;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.TrendDao;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.Trend;
import org.n52.eventing.wv.model.i18n.I18nTrend;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateTrendDao extends BaseHibernateDao<Trend> implements TrendDao {

    public HibernateTrendDao(Session s) {
        super(s);
    }

    @Override
    public Optional<Trend> retrieveByDomainTrend(DomainTrend code) throws DatabaseException {
        switch (code) {
            case LessLess:
                return retrieveById(11);
            case LessEqual:
                return retrieveById(12);
            case LessGreater:
                return retrieveById(13);
            case EqualLess:
                return retrieveById(21);
            case EqualEqual:
                return retrieveById(22);
            case EqualGreater:
                return retrieveById(23);
            case GreaterLess:
                return retrieveById(31);
            case GreaterEqual:
                return retrieveById(32);
            case GreaterGreater:
                return retrieveById(33);
            case Failure:
                return retrieveById(99);
            default:
                break;
        }

        return Optional.empty();
    }

    @Override
    public Optional<I18nTrend> retrieveAsLocale(Locale locale, Trend original) throws DatabaseException {
        String param = "identifier";
        String localeParam = "localeParam";
        String entity = I18nTrend.class.getSimpleName();
        String hql = String.format("SELECT ti FROM %s ti join ti.trendCode t WHERE t.code=:%s AND ti.locale=:%s", entity, param, localeParam);
        Query q = getSession().createQuery(hql);
        q.setParameter(param, original.getCode());
        q.setParameter(localeParam, locale.getLanguage());
        return q.list().stream().findFirst();
    }


}
