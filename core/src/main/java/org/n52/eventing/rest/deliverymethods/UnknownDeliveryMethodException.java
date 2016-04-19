package org.n52.eventing.rest.deliverymethods;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UnknownDeliveryMethodException extends Exception {

    public UnknownDeliveryMethodException(String message) {
        super(message);
    }

    public UnknownDeliveryMethodException(String message, Throwable cause) {
        super(message, cause);
    }

}
