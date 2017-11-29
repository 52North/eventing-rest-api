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
package org.n52.eventing.rest.binding;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.n52.eventing.rest.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class RequestUtils implements InitializingBean {

    @Autowired
    private Configuration configuration;

    private String xForwardedForHeader;
    private String xForwardedContextPathHeader;
    private String xForwardedHostHeader;
    private String xForwardedProtoHeader;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.xForwardedForHeader = configuration.getParameter("xForwardedForHeader").orElse("X-Forwarded-For");
        this.xForwardedHostHeader = configuration.getParameter("xForwardedHostHeader").orElse("X-Forwarded-Host");
        this.xForwardedProtoHeader = configuration.getParameter("xForwardedProtoHeader").orElse("X-Forwarded-Proto");
        this.xForwardedContextPathHeader = configuration.getParameter("xForwardedContextPathHeader").orElse("X-Forwarded-ContextPath");
    }

    public String getXForwardedForHeader() {
        return xForwardedForHeader;
    }

    public String getXForwardedContextPathHeader() {
        return xForwardedContextPathHeader;
    }

    public String getXForwardedProtoHeader() {
        return xForwardedProtoHeader;
    }

    public String getXForwardedHostHeader() {
        return xForwardedHostHeader;
    }

    public String resolveFullRequestUrl() throws IOException, URISyntaxException {
        HttpServletRequest request = resolveRequestObject();

        return resolveFullRequestUrl(request);
    }

    public String resolveFullRequestUrl(HttpServletRequest request) throws MalformedURLException, URISyntaxException {
        URL url = new URL(request.getRequestURL().toString());

        String scheme = url.getProtocol();
        String userInfo = url.getUserInfo();
        String host = url.getHost();

        String xForwardedForContext = request.getHeader(xForwardedContextPathHeader);
        String xForwardedHost = request.getHeader(xForwardedHostHeader);
        String xForwardedScheme = request.getHeader(xForwardedProtoHeader);

        if (xForwardedHost != null && !xForwardedHost.isEmpty()) {
            host = xForwardedHost.trim();
        }

        if (xForwardedScheme != null && !xForwardedScheme.isEmpty()) {
            scheme = xForwardedScheme.trim();
        }

        int port = url.getPort();

        String path = request.getRequestURI();
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String actualContext = request.getContextPath();
        if (xForwardedForContext != null && path != null) {
            path = path.replace(actualContext, xForwardedForContext.trim());
        }

        URI uri = new URI(scheme, userInfo, host, port, path, null, null);
        return uri.toString();
    }

    public HttpServletRequest resolveRequestObject() {
        return ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public HttpServletResponse resolveResponseObject() {
        return ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getResponse();
    }

}
