package org.n52.eventing.rest.subscriptions;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface SubscriptionManager {

    String subscribe(SubscriptionDefinition subDef) throws InvalidSubscriptionException;

    void updateSubscription(SubscriptionUpdateDefinition subDef) throws InvalidSubscriptionException;

}
