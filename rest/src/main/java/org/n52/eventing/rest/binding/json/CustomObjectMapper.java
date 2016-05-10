
package org.n52.eventing.rest.binding.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        super();
        this.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
