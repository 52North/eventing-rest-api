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

import java.util.Optional;
import org.hibernate.Session;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.dao.RuleDao;
import org.n52.eventing.wv.model.Trend;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateRuleDao extends BaseHibernateDao<Rule> implements RuleDao {

    public HibernateRuleDao(Session session) {
        super(session);
    }



    @Override
    public void removeRule(Rule r) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(Rule o) throws DatabaseException {
        o.setTrendCode(resolveTrend(o.getTrendCode()));
        super.store(o);
    }

    private Trend resolveTrend(Trend trendCode) {
        Session s = getSession();
        Trend retrieved = s.get(Trend.class, trendCode.getCode());
        if (retrieved != null) {
            return retrieved;
        }

        return trendCode;
    }




}
