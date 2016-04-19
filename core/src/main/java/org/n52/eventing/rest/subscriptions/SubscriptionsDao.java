package org.n52.eventing.rest.subscriptions;

import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface SubscriptionsDao {

    boolean hasSubscription(String id);

    List<Subscription> getSubscriptions();

    Subscription getSubscription(String id) throws UnknownSubscriptionException;

    void addSubscription(String subId, Subscription subscription);

    void updateEndOfLife(String id, DateTime eol) throws UnknownSubscriptionException;

    void updateStatus(String id, Subscription.Status status) throws UnknownSubscriptionException;

    void remove(String id) throws UnknownSubscriptionException;

}
