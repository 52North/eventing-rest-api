
package org.n52.eventing.rest;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ResourceCollection {

    private String id;
    private String label;
    private String description;
    private Integer size;

    private ResourceCollection(String id) {
        this.id = id;
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

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public ResourceCollection withLabel(String label) {
        this.label = label;
        return this;
    }

    public ResourceCollection withDescription(String description) {
        this.description = description;
        return this;
    }

    public ResourceCollection withCount(Integer count) {
        this.size = count;
        return this;
    }

    public static ResourceCollection createResource(String id) {
        return new ResourceCollection(id);
    }
}
