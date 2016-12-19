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

package org.n52.eventing.wv.security;

import org.n52.eventing.wv.model.UserWrapper;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.n52.eventing.rest.users.User;
import org.n52.eventing.security.NotAuthenticatedException;
import org.n52.eventing.security.SecurityService;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.n52.eventing.wv.dao.UserDao;
import org.n52.eventing.wv.dao.hibernate.HibernateUserDao;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;

/**
 * @since 4.0.0
 *
 */
public class UserSecurityService implements AuthenticationProvider, Serializable, SecurityService {
    private static final long serialVersionUID = -3207103212342510378L;

    private static final Logger LOG = LoggerFactory.getLogger(UserSecurityService.class);

    @Autowired
    private GroupPolicies groupPolicies;

    @Autowired
    private HibernateDatabaseConnection hdc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void setDatabaseConnection(HibernateDatabaseConnection hdc) {
        this.hdc = hdc;
    }

    @Override
    public User resolveCurrentUser() throws NotAuthenticatedException {
        Optional<WvUser> result = resolveCurrentWvUser();
        if (result.isPresent()) {
            return new UserWrapper(result.get(), containsAdminGroup(result.get().getGroups()));
        }

        throw new NotAuthenticatedException("No valid user object found");
    }

    public Optional<WvUser> resolveCurrentWvUser() throws NotAuthenticatedException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            String username;

            if (principal instanceof UserPrinciple) {
                username = ((UserPrinciple)principal).getUser().getName();
            } else {
                username = principal.toString();
            }

            Session session = hdc.createSession();
            UserDao userDao = new HibernateUserDao(session);

            try {
                Optional<WvUser> result = userDao.retrieveByName(username);
                return result;
            }
            finally {
                session.close();
            }
        }
        throw new NotAuthenticatedException("No valid user object found");
    }

    @Override
    public UsernamePasswordAuthenticationToken authenticate(Authentication authentication)
            throws AuthenticationException {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
        WvUser user = authenticate((String) auth.getPrincipal(), (String) auth.getCredentials());
        return new UsernamePasswordAuthenticationToken(new UserPrinciple(user,
                containsAdminGroup(user.getGroups())), null, createPrincipals(user.getGroups()));
    }

    public WvUser authenticate(final String username, final String password) throws AuthenticationException {
        if (username == null || password == null) {
            throw new BadCredentialsException("Invalid Credentials");
        }

        Session session = hdc.createSession();
        UserDao userDao = new HibernateUserDao(session);

        Optional<WvUser> user;
        try {
            user = userDao.retrieveByName(username);
        }
        finally {
            session.close();
        }


        if (user == null || !user.isPresent()) {
            throw new BadCredentialsException("Invalid Credentials");
        }

        if (!getPasswordEncoder().matches(password, user.get().getPassword())) {
            throw new BadCredentialsException("Invalid Credentials");
        }

        return user.get();
    }

    @Override
    public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }


    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setGroupPolicies(GroupPolicies groupPolicies) {
        this.groupPolicies = groupPolicies;
    }

    private boolean containsAdminGroup(Set<Group> groups) {
        if (groups == null) {
            return false;
        }

        return groups.stream()
                .filter((Group g) ->  groupPolicies.getAdminGroupNames().contains(g.getName()))
                .count() > 0;
    }

    private Collection<? extends GrantedAuthority> createPrincipals(Set<Group> groups) {
        if (groups == null) {
            return Collections.singletonList(new GroupPrinciple("users"));
        }
        return groups.stream()
                .map((Group t) -> {
                    if (groupPolicies.getAdminGroupNames().contains(t.getName())) {
                        return new GroupPrinciple("admins");
                    }
                    if (groupPolicies.getEditorGroupNames().contains(t.getName())) {
                        return new GroupPrinciple("editors");
                    }
                    return null;
                })
                .filter(gp -> gp != null)
                .distinct()
                .collect(Collectors.toList());
    }


}
