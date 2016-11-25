package org.n52.eventing.wv.dao;

import java.util.List;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface SubscriptionDao {

    void store(WvSubscription sub) throws DatabaseException;

    List<WvSubscription> retrieve(Pagination pagination) throws DatabaseException;

    List<WvSubscription> retrieveSubscriptions(WvUser user) throws DatabaseException;

    List<WvSubscription> retrieveSubscriptions(Group group) throws DatabaseException;

    void removeSubscription(WvSubscription sub) throws DatabaseException;

}
