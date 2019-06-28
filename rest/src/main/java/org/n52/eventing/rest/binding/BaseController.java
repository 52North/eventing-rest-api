/*
 * Copyright (C) 2016-2019 52°North Initiative for Geospatial Open Source
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
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.eventing.rest.binding.exception.BadQueryParameterException;
import org.n52.eventing.rest.binding.exception.BadRequestException;
import org.n52.eventing.rest.binding.exception.ExceptionResponse;
import org.n52.eventing.rest.binding.exception.InternalServerException;
import org.n52.eventing.rest.binding.exception.ResourceNotFoundException;
import org.n52.eventing.rest.binding.exception.WebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
public abstract class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesController.class);
    private static final String REFER_TO_API_SYNTAX = "Refer to the API documentation and check parameter "
            + "value against required syntax!";

    private static final String INVALID_REQUEST_BODY = "Check the request body which has been sent to the "
            + "server. Probably it is not valid.";

    @ExceptionHandler(value = {
            BadRequestException.class,
            BadQueryParameterException.class
        })
        public void handle400(Exception e, HttpServletRequest request, HttpServletResponse response) {
            writeExceptionResponse((WebException) e, response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(value = ResourceNotFoundException.class)
        public void handle404(Exception e, HttpServletRequest request, HttpServletResponse response) {
            writeExceptionResponse((WebException) e, response, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(value = InternalServerException.class)
        public void handle500(Exception e, HttpServletRequest request, HttpServletResponse response) {
            writeExceptionResponse((WebException) e, response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(value = {
            RuntimeException.class,
            Exception.class,
            Throwable.class
        })
        public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
            if (e instanceof HttpMessageNotReadableException) {
                WebException ex = new BadRequestException("Invalid Request", e).addHint(INVALID_REQUEST_BODY)
                                                                               .addHint(REFER_TO_API_SYNTAX);
                writeExceptionResponse(ex, response, HttpStatus.BAD_REQUEST);
            } else {
                WebException wrappedException = new InternalServerException("Unexpected Exception occured.", e);
                writeExceptionResponse(wrappedException, response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    private void writeExceptionResponse(WebException e, HttpServletResponse response, HttpStatus status) {

        final String logMessage = "An exception occured.";
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            LOGGER.error(logMessage, e);
        } else {
            LOGGER.debug(logMessage, e);
        }

        // TODO consider using a 'suppress_response_codes=true' parameter and always return 200 OK
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = createObjectMapper();
        ObjectWriter writer = objectMapper.writerFor(ExceptionResponse.class);
        ExceptionResponse exceptionResponse = ExceptionResponse.createExceptionResponse(e, status);
        try (OutputStream outputStream = response.getOutputStream()) {
            writer.writeValue(outputStream, exceptionResponse);
        } catch (IOException ioe) {
            LOGGER.error("Could not process error message.", ioe);
        }
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


}
