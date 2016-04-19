
package org.n52.eventing.rest.binding.templates;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.binding.UrlSettings;
import org.n52.eventing.rest.binding.EmptyArrayModel;
import org.n52.eventing.rest.templates.Template;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
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
@RequestMapping(value = UrlSettings.API_V1_BASE+"/"+UrlSettings.TEMPLATES_RESOURCE,
        produces = {"application/json"})
public class TemplatesController {

    @Autowired
    private TemplatesDao dao;

    @RequestMapping("")
    public ModelAndView getTemplates() throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        List<ResourceCollection> list = new ArrayList<>();
        this.dao.getTemplates().stream().forEach(t -> {
            list.add(ResourceCollection.createResource(t.getId())
                .withLabel(t.getLabel())
                .withDescription(t.getDescription())
                .withHref(String.format("%s/%s", fullUrl, t.getId())));
        });

        if (list.isEmpty()) {
            return EmptyArrayModel.create();
        }

        return new ModelAndView().addObject(list);
    }

    @RequestMapping("/{item}")
    public Template getTemplate(@PathVariable("item") String id) throws ResourceNotAvailableException {
        if (this.dao.hasTemplate(id)) {
            try {
                return this.dao.getTemplate(id);
            } catch (UnknownTemplateException ex) {
                throw new ResourceNotAvailableException(ex.getMessage(), ex);
            }
        }

        throw new ResourceNotAvailableException("not there: "+ id);
    }

}
