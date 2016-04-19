
package org.n52.eventing.rest.binding.subscriptions;

import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.subscriptions.SubscriptionsDao;
import org.n52.eventing.rest.subscriptions.UnknownSubscriptionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.SUBSCRIPTIONS_RESOURCE,
        produces = {"application/json"})
public class SubscriptionsController {

    @Autowired
    private SubscriptionsDao dao;

    @RequestMapping("")
    public ModelAndView getSubscriptions(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        return new ModelAndView().addObject(createSubscriptions(fullUrl));
    }

    private List<ResourceCollection> createSubscriptions(String fullUrl) {
        List<ResourceCollection> pubs = new ArrayList<>();

        this.dao.getSubscriptions().stream().forEach(s -> {
            String pubId = s.getId();
            pubs.add(ResourceCollection.createResource(pubId)
                    .withLabel(s.getLabel())
                    .withDescription(s.getDescription())
                    .withUserId(s.getUser().getId())
                    .withHref(String.format("%s/%s", fullUrl, pubId)));
        });

        return pubs;
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getSubscription(@RequestParam(required = false) MultiValueMap<String, String> query,
            @PathVariable("item") String id)
            throws IOException, URISyntaxException, ResourceNotAvailableException {

        if (!this.dao.hasSubscription(id)) {
            throw new ResourceNotAvailableException("The subscription is not available: "+id);
        }

        try {
            return new ModelAndView().addObject(this.dao.getSubscription(id));
        } catch (UnknownSubscriptionException ex) {
            throw new ResourceNotAvailableException(ex.getMessage(), ex);
        }
    }

}
