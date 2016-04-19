
package org.n52.eventing.rest.deliverymethods;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummyDeliveryMethodsDao implements DeliveryMethodsDao {

    private final DeliveryMethod dummyMethod;

    public DummyDeliveryMethodsDao() {
        this.dummyMethod = new DeliveryMethod("email", "Email", "Send email for every match", "email");
    }

    @Override
    public List<DeliveryMethod> getDeliveryMethods() {
        return Collections.singletonList(dummyMethod);
    }

    @Override
    public boolean hasDeliveryMethod(String id) {
        return dummyMethod.getId().equals(id);
    }

    @Override
    public DeliveryMethod getDeliveryMethod(String id) throws UnknownDeliveryMethodException {
        if (hasDeliveryMethod(id)) {
            return dummyMethod;
        }

        throw new UnknownDeliveryMethodException("Not there: "+id);
    }

}
