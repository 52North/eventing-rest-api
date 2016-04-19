
package org.n52.eventing.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = UrlSettings.API_V1_BASE, produces = {"application/json"})
public class ResourcesController {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesController.class);

    @RequestMapping("")
    public ModelAndView getResources(@RequestParam(required = false) MultiValueMap<String, String> query) throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        return new ModelAndView().addObject(createResources(fullUrl));
    }

    private Map<String, String> createResources(String fullUrl) {
        LOG.info("Full URL: {}", fullUrl);
        Map<String, String> resources = new HashMap<>();

        resources.put(UrlSettings.PUBLICATIONS_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.PUBLICATIONS_RESOURCE));
        resources.put(UrlSettings.DELIVERY_METHODS_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.DELIVERY_METHODS_RESOURCE));
        resources.put(UrlSettings.SUBSCRIPTIONS_RESOURCE,
                String.format("%s/%s", fullUrl, UrlSettings.SUBSCRIPTIONS_RESOURCE));

        return resources;
    }

}
