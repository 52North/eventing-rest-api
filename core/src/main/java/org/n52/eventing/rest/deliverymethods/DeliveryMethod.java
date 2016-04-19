package org.n52.eventing.rest.deliverymethods;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DeliveryMethod {

    private String id;
    private String label;
    private String description;
    private String dataType;

    public DeliveryMethod(String id, String label, String description, String dataType) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.dataType = dataType;
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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

}
