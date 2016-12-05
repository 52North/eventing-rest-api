/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.eventing.rest.binding.templates;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.n52.eventing.rest.binding.RequestUtils;
import org.n52.eventing.rest.binding.ResourceCollection;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.binding.EmptyArrayModel;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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


    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView getTemplates(@RequestParam(required = false) MultiValueMap<String, String> query) throws IOException, URISyntaxException, NotAuthenticatedException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();

        List<ResourceCollection> list = new ArrayList<>();

        List<TemplateDefinition> result = query == null ? this.dao.getTemplates() : this.dao.getTemplates(query);

        result.stream().forEach(t -> {
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

    @RequestMapping(value = "/{item}", method = RequestMethod.GET)
    public TemplateDefinition getTemplate(@PathVariable("item") String id) throws ResourceNotAvailableException, NotAuthenticatedException {
        if (this.dao.hasTemplate(id)) {
            try {
                TemplateDefinition temp = this.dao.getTemplate(id);
                return temp;
            } catch (UnknownTemplateException ex) {
                throw new ResourceNotAvailableException(ex.getMessage(), ex);
            }
        }

        throw new ResourceNotAvailableException("not there: "+ id);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ModelAndView create(@RequestBody TemplateDefinition def) {
        String id = this.dao.createTemplate(def);
        ModelAndView result = new ModelAndView();
        result.addObject(Collections.singletonMap("id", id));
        return result;
    }

}
