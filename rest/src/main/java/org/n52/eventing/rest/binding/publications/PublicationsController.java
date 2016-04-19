
package org.n52.eventing.rest.binding.publications;

import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.publications.PublicationsDao;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
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
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.PUBLICATIONS_RESOURCE,
        produces = {"application/json"})
public class PublicationsController {

    @Autowired
    private PublicationsDao dao;

    @RequestMapping("")
    public ModelAndView getPublications(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        return new ModelAndView().addObject(createPublications(fullUrl));
    }

    private List<ResourceCollection> createPublications(String fullUrl) {
        List<ResourceCollection> pubs = new ArrayList<>();

        this.dao.getPublications().stream().forEach(p -> {
            String pubId = p.getId();
            pubs.add(ResourceCollection.createResource(pubId)
                    .withLabel(p.getLabel())
                    .withDescription(p.getDescription())
                    .withHref(String.format("%s/%s", fullUrl, pubId)));
        });

        return pubs;
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getPublication(@RequestParam(required = false) MultiValueMap<String, String> query,
            @PathVariable("item") String id)
            throws IOException, URISyntaxException, ResourceNotAvailableException {

        if (!this.dao.hasPublication(id)) {
            throw new ResourceNotAvailableException("The publication is not available: "+id);
        }

        try {
            return new ModelAndView().addObject(this.dao.getPublication(id));
        } catch (UnknownPublicationsException ex) {
            throw new ResourceNotAvailableException(ex.getMessage(), ex);
        }
    }

}
