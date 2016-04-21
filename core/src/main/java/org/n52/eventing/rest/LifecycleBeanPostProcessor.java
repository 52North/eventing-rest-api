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
package org.n52.eventing.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;


/**
 * Bean post processor that calls {@link Constructable#construct() } and
 * {@link Destroyable#destroy()} for every bean that implements these
 * interfaces. In contrast to the {@link javax.annotation.PostConstruct} and
 * {@link javax.annotation.PreDestroy} annotations, these methods will also be
 * called if they are declared in a super class of the bean.
 *
 * This postprocess will be called at the same stages,
 * {@link CommonAnnotationBeanPostProcessor} would be called:
 * Constructor
 * Bean-Injections
 * Settings-Injections
 * construct()
 * ...
 * destroy()
 *
 * @see Constructable
 * @see Destroyable
 *
 * @since 1.0.0
 *
 * @author Christian Autermann
 */
public class LifecycleBeanPostProcessor
        implements DestructionAwareBeanPostProcessor, PriorityOrdered {
    private static final Logger LOG = LoggerFactory
            .getLogger(LifecycleBeanPostProcessor.class);

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof Constructable) {
            try {
                ((Constructable) bean).construct();
            } catch (Throwable t) {
                throw new BeanInitializationException(
                        "Couldn't counstruct bean " + beanName, t);
            }
        }

        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        if (bean instanceof Destroyable) {

            try {
                ((Destroyable) bean).destroy();
            } catch (Throwable t) {
                LOG.error("Couldn't invoke destroy method on " + beanName, t);
            }

        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }
}
