
package org.n52.eventing.rest;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = "/v1", produces = {"application/json"})
public class ResourcesController {

    @RequestMapping("")
    public ModelAndView getResources(@RequestParam(required = false) MultiValueMap<String, String> query) {
        return new ModelAndView().addObject(createResources());
    }

    private List<ResourceCollection> createResources() {
        List<ResourceCollection> resources = new ArrayList<>();
        ResourceCollection publications = ResourceCollection.createResource("publications").withLabel("The available publications").withDescription("The publications that this eventing API provides for subscriptions");
        ResourceCollection deliveryMethods = ResourceCollection.createResource("deliveryMethods").withLabel("The available delivery methods").withDescription("The delivery methods that this eventing API provides for subscriptions");

        resources.add(publications);
        resources.add(deliveryMethods);

        return resources;
    }

}
