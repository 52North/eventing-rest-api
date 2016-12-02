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

package org.n52.eventing.wv.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.n52.eventing.rest.UrlSettings;
import org.n52.eventing.security.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.n52.eventing.wv.dao.hibernate.HibernateUserDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvUser;
import org.n52.eventing.wv.security.AccessRights;
import org.n52.eventing.wv.security.UserSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(value = UrlSettings.API_V1_BASE+"/users",
        produces = {"application/json"})
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HibernateDatabaseConnection hdc;

    @Autowired
    private AccessRights accessRights;

    @Autowired
    private UserSecurityService userService;


    @RequestMapping("")
    public List<UserView> getUsers(@RequestParam(required = false) MultiValueMap<String, String> query)
            throws IOException, URISyntaxException, NotAuthenticatedException {
        try (Session session = hdc.createSession()) {
            HibernateUserDao dao = new HibernateUserDao(session);
            Optional<WvUser> u = userService.resolveCurrentWvUser();

            if (!u.isPresent()) {
                throw new NotAuthenticatedException("No user present");
            }

            return dao.retrieve(null).stream()
                    .filter(g -> accessRights.canSeeSubscriptionsOfUser(u.get(), g))
                    .map((WvUser wu) -> {
                        Hibernate.initialize(wu.getGroups());
                        return UserView.from(wu);
                    })
                    .collect(Collectors.toList());
        }
    }

    @RequestMapping(value = "/{item}", method = GET)
    public UserView getUser(@RequestParam(required = false) MultiValueMap<String, String> query,
            @PathVariable("item") String id)
            throws IOException, URISyntaxException, NotAuthenticatedException {
        try (Session session = hdc.createSession()) {
            HibernateUserDao dao = new HibernateUserDao(session);
            Optional<WvUser> u = userService.resolveCurrentWvUser();

            if (!u.isPresent()) {
                throw new NotAuthenticatedException("No user present");
            }

            Optional<WvUser> result = dao.retrieveById(Integer.parseInt(id));
            if (result.isPresent()) {
                if (!accessRights.canSeeSubscriptionsOfUser(u.get(), result.get())) {
                    throw new NotAuthenticatedException("Access denied");
                }

                Hibernate.initialize(result.get().getGroups());
                return UserView.from(result.get());
            }
            else {
                return null;
            }
        }
        catch (NumberFormatException e) {
            LOG.warn(e.getMessage());
            throw new NumberFormatException("invalid ID provided. IDs must be an integer");
        }
    }

    public static class UserView {

        private final int id;
        private final String name;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final Set<Group> groups;

        public static UserView from(WvUser wu) {
            return new UserView(wu.getId(), wu.getName(),
                    wu.getFirstName(), wu.getLastName(),
                    wu.getEmail(), wu.getGroups());
        }

        private UserView(int id, String name, String firstName, String lastName, String email, Set<Group> groups) {
            this.id = id;
            this.name = name;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.groups = groups;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public Set<Group> getGroups() {
            return groups;
        }

    }

}
