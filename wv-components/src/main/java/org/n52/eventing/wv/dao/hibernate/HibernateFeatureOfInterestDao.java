
package org.n52.eventing.wv.dao.hibernate;

import org.hibernate.Session;
import org.n52.eventing.wv.dao.BaseDao;
import org.n52.eventing.wv.model.FeatureOfInterest;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateFeatureOfInterestDao extends BaseHibernateDao<FeatureOfInterest> implements BaseDao<FeatureOfInterest> {

    public HibernateFeatureOfInterestDao(Session session) {
        super(session);
    }

}
