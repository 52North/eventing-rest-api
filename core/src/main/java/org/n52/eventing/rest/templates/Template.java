
package org.n52.eventing.rest.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Template {

    private String id;
    private String label;
    private String description;
    private Definition definition;
    private List<Parameter> parameters = new ArrayList<>();

    public Template(String id, String label, String description, Definition definition) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.definition = definition;
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

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    void addParameter(String name, String dataType) {
        this.parameters.add(new Parameter(name, dataType));
    }

}
