
package org.n52.eventing.rest.binding.deliverymethods;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.deliverymethods.DeliveryMethod;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsDao;
import org.n52.eventing.rest.deliverymethods.UnknownDeliveryMethodException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.DELIVERY_METHODS_RESOURCE,
        produces = {"application/json"})
public class DeliveryMethodsController {

    @Autowired
    private DeliveryMethodsDao dao;

    @RequestMapping("")
    public ModelAndView getDeliveryMethods() throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        List<ResourceCollection> list = new ArrayList<>();

        this.dao.getDeliveryMethods().stream().forEach(dm -> {
            list.add(ResourceCollection.createResource(dm.getId())
                .withLabel(dm.getLabel())
                .withDescription(dm.getDescription())
                .withHref(String.format("%s/%s", fullUrl, dm.getId())));
        });

        return new ModelAndView().addObject(list);
    }

    @RequestMapping("/{item}")
    public DeliveryMethod getDeliveryMethod(@PathVariable("item") String id) throws ResourceNotAvailableException {
        if (this.dao.hasDeliveryMethod(id)) {
            try {
                return this.dao.getDeliveryMethod(id);
            } catch (UnknownDeliveryMethodException ex) {
                throw new ResourceNotAvailableException(ex.getMessage(), ex);
            }
        }

        throw new ResourceNotAvailableException("Delivery method not available: "+id);
    }

}
