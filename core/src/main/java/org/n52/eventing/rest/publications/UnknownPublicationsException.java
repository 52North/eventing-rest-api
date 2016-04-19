
package org.n52.eventing.rest.publications;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UnknownPublicationsException extends Exception {

    public UnknownPublicationsException(String message) {
        super(message);
    }

    public UnknownPublicationsException(String message, Throwable cause) {
        super(message, cause);
    }

}
