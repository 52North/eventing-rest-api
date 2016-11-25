
package org.n52.eventing.wv.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Phenomenon {

    private int id;
    private String phenomenonId;

    public Phenomenon() {
    }

    public Phenomenon(String phenomenonId) {
        this.phenomenonId = phenomenonId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhenomenonId() {
        return phenomenonId;
    }

    public void setPhenomenonId(String phenomenonId) {
        this.phenomenonId = phenomenonId;
    }

}
