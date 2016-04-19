
package org.n52.eventing.rest.subscriptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.n52.eventing.rest.publications.PublicationsDao;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.n52.eventing.rest.users.UnknownUserException;
import org.n52.eventing.rest.users.UsersDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummySubscriptionsDao implements SubscriptionsDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(DummySubscriptionsDao.class);
    private final Map<String, Subscription> subscriptions = new HashMap<>();
    
    private PublicationsDao publicationsDao;
    private UsersDao usersDao;
    
    public DummySubscriptionsDao(UsersDao usersDao, PublicationsDao publicationsDao) {
        this.usersDao = usersDao;
        this.publicationsDao = publicationsDao;
        LOG.info("initializing subscriptions...");
        
        try {
            Subscription sub = new Subscription("dummy-sub", "dummy-sub yeah", "this subscription is set up!");
            sub.setUser(this.usersDao.getUser("dummy-user"));
            sub.setPublicationId(this.publicationsDao.getPublication("dummy-pub").getId());
            sub.setDeliveryMethodId("dummy-delivery");
            sub.setEndOfLife(new Date(System.currentTimeMillis()+1000000));
            sub.setStatus(Subscription.Status.ENABLED);
            subscriptions.put("dummy-sub", sub);
        } catch (UnknownPublicationsException | UnknownUserException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }
    
    @Override
    public synchronized boolean hasSubscription(String id) {
        return subscriptions.containsKey(id);
    }
    
    @Override
    public synchronized List<Subscription> getSubscriptions() {
        return Collections.unmodifiableList(new ArrayList<>(subscriptions.values()));
    }
    
    @Override
    public synchronized Subscription getSubscription(String id) throws UnknownSubscriptionException {
        if (!hasSubscription(id)) {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }
        
        return subscriptions.get(id);
    }
    
}
