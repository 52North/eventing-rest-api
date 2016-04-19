
package org.n52.eventing.rest.binding.subscriptions;

import org.n52.eventing.rest.binding.EmptyArrayModel;
import org.n52.eventing.rest.subscriptions.SubscriptionUpdateDefinition;
import org.n52.eventing.rest.subscriptions.SubscriptionDefinition;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.n52.eventing.rest.subscriptions.SubscriptionManager;
import org.n52.eventing.rest.subscriptions.SubscriptionsDao;
import org.n52.eventing.rest.subscriptions.UnknownSubscriptionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
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

    @Autowired
    private SubscriptionManager manager;

    @RequestMapping("")
    public ModelAndView getSubscriptions(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();

        List<ResourceCollection> subs = createSubscriptions(fullUrl);

        if (subs.isEmpty()) {
            return EmptyArrayModel.create();
        }

        return new ModelAndView().addObject(subs);
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


    @RequestMapping(value = "", method = POST)
    public ModelAndView subscribe(@RequestBody SubscriptionDefinition subDef) throws InvalidSubscriptionException {
        String subId = this.manager.subscribe(subDef);

        return new ModelAndView().addObject(Collections.singletonMap("id", subId));
    }

    @RequestMapping(value = "/{item}", method = PUT)
    public ResponseEntity<?> updateSubscription(@RequestBody SubscriptionUpdateDefinition subDef,
            @PathVariable("item") String id) throws InvalidSubscriptionException {
        subDef.setId(id);
        this.manager.updateSubscription(subDef);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{item}", method = DELETE)
    public ResponseEntity<?> remove(@PathVariable("item") String id) throws InvalidSubscriptionException {
        this.manager.removeSubscription(id);

        return ResponseEntity.accepted().build();
    }

}
