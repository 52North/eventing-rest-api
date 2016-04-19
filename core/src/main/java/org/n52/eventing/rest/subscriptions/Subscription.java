
package org.n52.eventing.rest.subscriptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.eventing.rest.users.User;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Subscription {

    public enum Status {
        ENABLED,
        DISABLED
    }

    private String id;
    private String label;
    private String description;
    private User user;
    private String publicationId;
    private String templateId;
    private String deliveryMethodId;
    private Status status;
    private String endOfLife;
    private List<Map<String, Object>> parameters = new ArrayList<>();

    public Subscription() {
        Map<String, Object> param1 = new HashMap<>();
        param1.put("waterGauge", 1.44);
        parameters.add(param1);
    }

    public Subscription(String id, String label, String description) {
        this();
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getDeliveryMethodId() {
        return deliveryMethodId;
    }

    public void setDeliveryMethodId(String deliveryMethodId) {
        this.deliveryMethodId = deliveryMethodId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(String endOfLife) {
        this.endOfLife = endOfLife;
    }

    public List<Map<String, Object>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Map<String, Object>> parameters) {
        this.parameters = parameters;
    }



}
