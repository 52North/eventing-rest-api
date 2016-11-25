
package org.n52.eventing.wv.dao.hibernate;

import org.hibernate.Session;
import org.n52.eventing.wv.dao.SeriesDao;
import org.n52.eventing.wv.model.Series;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateSeriesDao extends BaseHibernateDao<Series> implements SeriesDao {

    public HibernateSeriesDao(Session session) {
        super(session);
    }


}
