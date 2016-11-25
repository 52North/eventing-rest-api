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

package org.n52.eventing.wv.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.n52.eventing.rest.templates.Definition;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.RuleDao;
import org.n52.eventing.wv.dao.hibernate.HibernateRuleDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class TemplatesService implements TemplatesDao {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatesService.class);

    @Autowired
    private HibernateDatabaseConnection hdc;

    @Override
    public boolean hasTemplate(String id) {
        Session session = hdc.createSession();

        RuleDao dao = new HibernateRuleDao(session);
        try {
            Optional<Rule> templ = dao.retrieveById(Integer.parseInt(id));
            return templ.isPresent();
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }
        finally {
            session.close();
        }

        return false;
    }

    @Override
    public TemplateDefinition getTemplate(String id) throws UnknownTemplateException {
        Session session = hdc.createSession();

        RuleDao dao = new HibernateRuleDao(session);
        try {
            Optional<Rule> templ = dao.retrieveById(Integer.parseInt(id));
            if (templ.isPresent()) {
                return wrapRule(templ.get());
            }
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
        }
        finally {
            session.close();
        }

        throw new UnknownTemplateException("Template not availabled: "+id);
    }

    @Override
    public List<TemplateDefinition> getTemplates() {
        Session session = hdc.createSession();

        RuleDao dao = new HibernateRuleDao(session);
        try {
            List<Rule> templ = dao.retrieve(null);
            return templ.stream().map((Rule r) -> {
                return wrapRuleBrief(r);
            }).collect(Collectors.toList());
        }
        catch (DatabaseException | NumberFormatException e) {
            LOG.warn(e.getMessage());
        }
        finally {
            session.close();
        }

        return Collections.emptyList();
    }

    private TemplateDefinition wrapRule(Rule r) {
        String label = String.format("Rule '%s' for Series '%s'", r.getId(), r.getSeries().getId());
        String desc = String.format("Rule '%s' for Series '%s': '%s' of feature '%s' using procedure '%s'",
                r.getId(),
                r.getSeries().getId(),
                r.getSeries().getPhenomenon().getPhenomenonId(),
                r.getSeries().getFeature().getIdentifier(),
                r.getSeries().getProcedure().getProcedureId());
        TemplateDefinition result = new TemplateDefinition(Integer.toString(r.getId()), label, desc, null);
        Map<String, Object> props = new HashMap<>();
        props.put("feature", r.getSeries().getFeature().getIdentifier());
        props.put("category", r.getSeries().getCategory().getCategoryId());
        props.put("phenomenon", r.getSeries().getPhenomenon().getPhenomenonId());
        props.put("procedure", r.getSeries().getProcedure().getProcedureId());
        result.setDefinition(new Definition(props, null));
        return result;
    }

    private TemplateDefinition wrapRuleBrief(Rule r) {
        String desc = String.format("Rule '%s' for Series '%s'", r.getId(), r.getSeries().getId());
        TemplateDefinition result = new TemplateDefinition(Integer.toString(r.getId()), desc, null, null);
        return result;
    }

}
