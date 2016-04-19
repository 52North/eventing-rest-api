
package org.n52.eventing.rest.subscriptions;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UnknownSubscriptionException extends Exception {

    public UnknownSubscriptionException(String message) {
        super(message);
    }

    public UnknownSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

}
