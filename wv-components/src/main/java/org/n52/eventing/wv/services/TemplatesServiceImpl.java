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
import org.hibernate.Transaction;
import org.n52.eventing.rest.RequestContext;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.rest.templates.TemplateDefinition;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.RuleDao;
import org.n52.eventing.wv.dao.hibernate.HibernateRuleDao;
import org.n52.eventing.wv.dao.hibernate.HibernateSeriesDao;
import org.n52.eventing.wv.dao.hibernate.HibernateTrendDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.i18n.I18nProvider;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.model.Series;
import org.n52.eventing.wv.model.Trend;
import org.n52.eventing.wv.model.WvTemplateDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class TemplatesServiceImpl implements TemplatesDao {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatesServiceImpl.class);

    private final I18nProvider i18n;
    private final HibernateDatabaseConnection hdc;
    private final RequestContext context;
    private final boolean expanded;

    public TemplatesServiceImpl(I18nProvider i18n, HibernateDatabaseConnection hdc, RequestContext context, boolean expanded) {
        this.i18n = i18n;
        this.hdc = hdc;
        this.context = context;
        this.expanded = expanded;
    }

    @Override
    public String createTemplate(TemplateDefinition def) {
        Session session = hdc.createSession();

        RuleDao dao = new HibernateRuleDao(session);
        try {

            Rule r = new Rule();
            Integer series = extractIntegerParameter(def, "publication");
            Number threshold = extractDoubleParameter(def, "threshold");
            Integer trendCode = extractIntegerParameter(def, "trend");
            r.setThreshold(threshold.doubleValue());

            Optional<Series> s = new HibernateSeriesDao(session).retrieveById(series);
            if (!s.isPresent()) {
                throw new IllegalArgumentException("Series is not known: "+series);
            }
            r.setSeries(s.get());

            Optional<Trend> t = new HibernateTrendDao(session).retrieveById(trendCode);
            if (!t.isPresent()) {
                throw new IllegalArgumentException("Trend is not supported: "+trendCode);
            }
            r.setTrendCode(t.get());

            if (dao.hasEntity(r)) {
                throw new IllegalArgumentException(i18n.getString("rule.alreadyPresent"));
            }

            Transaction trans = session.beginTransaction();
            dao.store(r);
            trans.commit();
            return Integer.toString(r.getId());
        }
        catch (DatabaseException e) {
            LOG.warn(e.getMessage());
            throw new RuntimeException("Error on storing rule", e);
        }
        finally {
            session.close();
        }
    }

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
            LOG.debug(e.getMessage(), e);
        }
        finally {
            session.close();
        }

        throw new UnknownTemplateException("Template not availabled: "+id);
    }

    @Override
    public List<TemplateDefinition> getTemplates() {
        return internalGet((Session s ) -> {
            RuleDao dao = new HibernateRuleDao(s);
            return dao.retrieve(null);
        });
    }


    @Override
    public List<TemplateDefinition> getTemplates(Map<String, String[]>  filter) {
        if (filter == null || filter.isEmpty() || !filter.containsKey("publication")) {
            return getTemplates();
        }
        return internalGet((Session s ) -> {
            RuleDao dao = new HibernateRuleDao(s);
            String[] val = filter.get("publication");
            if (val == null || val.length == 0) {
                throw new DatabaseException("Filter 'publication' cannot be empty");
            }
            return dao.retrieveBySeries(val[0].split(","));
        });
    }

    private List<TemplateDefinition> internalGet(DaoSupplier<List<Rule>> supplier) {
        try (Session session = hdc.createSession()) {
            List<Rule> templ = supplier.getFromDao(session);
            return templ.stream().map((Rule r) -> {
                return expanded ? wrapRule(r) : wrapRuleBrief(r);
            }).collect(Collectors.toList());
        }
        catch (DatabaseException | NumberFormatException | NotAuthenticatedException e) {
            LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private TemplateDefinition wrapRule(Rule r) {
        WvTemplateDefinition result = new WvSubscriptionTemplateFactory(i18n).createTemplate(r);
        Map<String, String> publicationMap = new HashMap<>();
        publicationMap.put("id", Integer.toString(r.getSeries().getId()));
        publicationMap.put("href", String.format("%s/%s/%s",
                context.getBaseApiUrl(),
                UrlSettings.PUBLICATIONS_RESOURCE,
                r.getSeries().getId()));
        result.setPublication(publicationMap);
        return result;
    }

    private TemplateDefinition wrapRuleBrief(Rule r) {
        String trendcodeLabel = i18n.getString("trendcode."+r.getTrendCode().getId());
        String label = String.format(i18n.getString("rule.label"),
                r.getSeries().getPhenomenon().getName(),
                r.getSeries().getFeature().getIdentifier(),
                trendcodeLabel,
                r.getThreshold());
        TemplateDefinition result = new TemplateDefinition(Integer.toString(r.getId()), label, null, null);
        return result;
    }

    private Integer extractIntegerParameter(TemplateDefinition def, String param) {
        try {
            return extractParameter(def, param);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("Wrong datatype for paramter: "+param);
        }
    }

    private Number extractDoubleParameter(TemplateDefinition def, String param) {
        try {
            return extractParameter(def, param);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("Wrong datatype for paramter: "+param);
        }
    }

    private <T> T extractParameter(TemplateDefinition def, String param) {
        if (def.getDefinition() != null && def.getDefinition().getContent() instanceof Map<?, ?>) {
            Map<?, ?> content = (Map<?, ?>) def.getDefinition().getContent();
            if (content.containsKey(param)) {
                return (T) content.get(param);
            }
        }

        throw new IllegalArgumentException("Required parameter not provided: "+param);
    }

}

