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

package org.n52.eventing.rest.binding;

import javax.servlet.http.HttpServletRequest;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@ControllerAdvice
public class ExceptionHandlerImpl {

    public static final String DEFAULT_ERROR_VIEW = "error";
    private static final String BACKLINK = "href";

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        return createModelAndView(e, req);
    }

    @ExceptionHandler(value = ResourceNotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView unknownResourceHandler(HttpServletRequest req, Exception e) throws Exception {
        return createModelAndView(e, req);
    }


    @ExceptionHandler(value = InvalidSubscriptionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView invalidSubscriptionHandler(HttpServletRequest req, Exception e) throws Exception {
        return createModelAndView(e, req);
    }


    private ModelAndView createModelAndView(Exception e, HttpServletRequest req) {
        ModelAndView mav = new ModelAndView();
        mav.addObject(DEFAULT_ERROR_VIEW, e.getMessage());
        mav.addObject(BACKLINK, req.getRequestURL());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
}
