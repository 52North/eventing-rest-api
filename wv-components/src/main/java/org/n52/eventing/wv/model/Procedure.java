
package org.n52.eventing.wv.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Procedure {

    private int id;
    private String procedureId;

    public Procedure() {
    }

    public Procedure(String procedureId) {
        this.procedureId = procedureId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(String procedureId) {
        this.procedureId = procedureId;
    }

}
