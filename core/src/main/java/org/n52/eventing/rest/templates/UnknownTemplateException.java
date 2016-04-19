
package org.n52.eventing.rest.templates;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UnknownTemplateException extends Exception {

    public UnknownTemplateException(String message) {
        super(message);
    }

    public UnknownTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

}
