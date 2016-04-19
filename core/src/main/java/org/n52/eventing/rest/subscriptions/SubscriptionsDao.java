package org.n52.eventing.rest.subscriptions;

import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface SubscriptionsDao {

    boolean hasSubscription(String id);

    List<Subscription> getSubscriptions();

    Subscription getSubscription(String id) throws UnknownSubscriptionException;

}
