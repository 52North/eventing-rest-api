
package org.n52.eventing.wv.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class FeatureOfInterest {
    
    private int id;
    private String identifier;
    private String name;
//    private Geometry geometry;
    private String featureClass;
    private int referenceId;
    private String description;

    public FeatureOfInterest() {
    }

    public FeatureOfInterest(String identifier, String name, String featureClass, int referenceId, String description) {
        this.identifier = identifier;
        this.name = name;
        this.featureClass = featureClass;
        this.referenceId = referenceId;
        this.description = description;
    }

    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    

}
