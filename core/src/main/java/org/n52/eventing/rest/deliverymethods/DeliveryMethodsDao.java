package org.n52.eventing.rest.deliverymethods;


import java.util.List;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface DeliveryMethodsDao {

    List<DeliveryMethod> getDeliveryMethods();

    boolean hasDeliveryMethod(String id);

    DeliveryMethod getDeliveryMethod(String id) throws UnknownDeliveryMethodException;

}
