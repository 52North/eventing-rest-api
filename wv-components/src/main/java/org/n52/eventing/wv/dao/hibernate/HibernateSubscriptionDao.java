
package org.n52.eventing.wv.dao.hibernate;

import java.util.List;
import org.hibernate.Session;
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
    public List<WvSubscription> retrieveSubscriptions(WvUser user) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<WvSubscription> retrieveSubscriptions(Group group) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSubscription(WvSubscription sub) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
