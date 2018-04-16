/*
 * Copyright (C) 2016-2017 52°North Initiative for Geospatial Open Source
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

import com.fasterxml.jackson.annotation.JsonView;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.n52.eventing.rest.InvalidPaginationException;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.QueryResult;
import org.n52.eventing.rest.RequestContext;
import org.n52.eventing.rest.binding.ResourceNotAvailableException;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.ResourceCollectionWithMetadata;
import org.n52.eventing.rest.binding.json.CustomObjectMapper;
import org.n52.eventing.rest.factory.TemplatesDaoFactory;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.rest.model.TemplateDefinition;
import org.n52.eventing.rest.model.views.Views;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private TemplatesDaoFactory daoFactory;

    @Autowired
    private RequestContext context;

    @Autowired
    private CustomObjectMapper mapper;


    @JsonView(Views.TemplateOverview.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResourceCollectionWithMetadata<TemplateDefinition> getTemplates() throws IOException, URISyntaxException, NotAuthenticatedException, InvalidPaginationException {
        RequestContext.storeInThreadLocal(context);

        Map<String, String[]> query = context.getParameters();
        Pagination page = Pagination.fromQuery(query);

        try {
            TemplatesDao dao = this.daoFactory.newDao();
            QueryResult<TemplateDefinition> result = query == null ? dao.getTemplates() : dao.getTemplates(query);

            return new ResourceCollectionWithMetadata<>(result.getResult(), new ResourceCollectionWithMetadata.Metadata(result.getTotalHits(), page));
        }
        finally {
            RequestContext.removeThreadLocal();
        }

    }

    @JsonView(Views.TemplateExpanded.class)
    @RequestMapping(path = "", params = {"expanded=true"})
    public ResourceCollectionWithMetadata<TemplateDefinition> getTemplatesExpanded() throws IOException, URISyntaxException, NotAuthenticatedException, InvalidPaginationException {
        return getTemplates();
    }

    @JsonView(Views.TemplateExpanded.class)
    @RequestMapping(value = "/{item}", method = RequestMethod.GET)
    public TemplateDefinition getTemplate(@PathVariable("item") String id) throws ResourceNotAvailableException, NotAuthenticatedException, IOException, URISyntaxException {
        RequestContext.storeInThreadLocal(context);

        try {
            TemplatesDao dao = this.daoFactory.newDao();
            if (dao.hasTemplate(id)) {
                try {
                    TemplateDefinition temp = dao.getTemplate(id);
                    return temp;
                } catch (UnknownTemplateException ex) {
                    throw new ResourceNotAvailableException(ex.getMessage(), ex);
                }
            }
        }
        finally {
            RequestContext.removeThreadLocal();
        }

        throw new ResourceNotAvailableException("not there: "+ id);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ModelAndView create(@RequestBody TemplateDefinition def) throws IOException, URISyntaxException {
        RequestContext.storeInThreadLocal(context);

        try {
            TemplatesDao dao = this.daoFactory.newDao();
            String id = dao.createTemplate(def);
            ModelAndView result = new ModelAndView();

            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("href", String.format("%s/%s",
                    context.getFullUrl(),
                    id));
            result.addObject(map);
            return result;
        }
        finally {
            RequestContext.removeThreadLocal();
        }

    }

}
