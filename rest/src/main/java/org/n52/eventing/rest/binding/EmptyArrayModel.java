
package org.n52.eventing.rest.binding;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class EmptyArrayModel {

    public static ModelAndView create() {
        return new ModelAndView().addObject(new EmptyArrayModel().list);
    }
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Object[] list = new Object[0];

    public EmptyArrayModel() {
    }

    public Object[] getList() {
        return list;
    }

    public void setList(Object[] list) {
        this.list = list;
    }

}
