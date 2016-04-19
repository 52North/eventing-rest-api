
package org.n52.eventing.rest.subscriptions;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionUpdateDefinition {

    private String id;
    private String status;
    private String endOfLife;

    public SubscriptionUpdateDefinition() {
    }

    public SubscriptionUpdateDefinition(String status, String endOfLife) {
        this.status = status;
        this.endOfLife = endOfLife;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(String endOfLife) {
        this.endOfLife = endOfLife;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
