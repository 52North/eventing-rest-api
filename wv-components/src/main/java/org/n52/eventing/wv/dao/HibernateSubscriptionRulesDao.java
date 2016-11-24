
package org.n52.eventing.wv.dao;

import java.util.List;
import java.util.Optional;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateSubscriptionRulesDao extends BaseHibernateDao implements SubscriptionRulesDao {
    
    
    
    @Override
    public void storeSubscription(WvSubscription sub) throws DatabaseException {
        internalPersist(sub);
    }

    @Override
    public List<WvSubscription> retrieveSubscriptions() throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    @Override
    public Optional<WvSubscription> retrieveSubscription(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<Rule> retrieveRule(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rule> retrieveRules() throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void storeRule(Rule r) throws DatabaseException {
        internalPersist(r);
    }

    @Override
    public void removeRule(Rule r) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
