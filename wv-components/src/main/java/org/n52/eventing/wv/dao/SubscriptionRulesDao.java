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
public interface SubscriptionRulesDao {
    
    void storeSubscription(WvSubscription sub) throws DatabaseException;
    
    List<WvSubscription> retrieveSubscriptions() throws DatabaseException;
    
    List<WvSubscription> retrieveSubscriptions(WvUser user) throws DatabaseException;
    
    List<WvSubscription> retrieveSubscriptions(Group group) throws DatabaseException;
    
    void removeSubscription(WvSubscription sub) throws DatabaseException;
    
    Optional<WvSubscription> retrieveSubscription(int id);
    
    Optional<Rule> retrieveRule(int id);
    
    List<Rule> retrieveRules() throws DatabaseException;
    
    void storeRule(Rule r) throws DatabaseException;
    
    void removeRule(Rule r) throws DatabaseException;
    
}
