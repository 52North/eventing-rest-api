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

import java.util.Set;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvSubscription;
import org.n52.eventing.wv.model.WvUser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AccessRightsImpl implements AccessRights, InitializingBean {

    @Autowired
    private GroupPolicies policies;
    private Set<String> adminGroups;

    public void setPolicies(GroupPolicies policies) {
        this.policies = policies;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.adminGroups = this.policies.getAdminGroupNames();
    }

    @Override
    public boolean canSeeSubscription(WvUser u, WvSubscription sub) {
        if (isInAdminGroup(u)) {
            return true;
        }

        if (sub.getUser() != null) {
            return canSeeSubscriptionsOfUser(u, sub.getUser());
        }

        if (sub.getGroup() != null) {
            return canSeeSubscriptionsOfGroup(u, sub.getGroup());
        }

        return false;
    }

    @Override
    public boolean canManageSubscription(WvUser u, WvSubscription sub) {
        if (isInAdminGroup(u)) {
            return true;
        }

        if (sub.getUser() != null) {
            return canManageSubscriptionsForUser(u, sub.getUser());
        }

        if (sub.getGroup() != null) {
            return canManageSubscriptionsForGroup(u, sub.getGroup());
        }

        return false;
    }

    @Override
    public boolean canSeeSubscriptionsOfGroup(WvUser u, Group g) {
        if (isInAdminGroup(u)) {
            return true;
        }

        return u.getGroups() != null && u.getGroups().contains(g);
    }

    @Override
    public boolean canSeeSubscriptionsOfUser(WvUser u1, WvUser u2) {
        if (u1.getId() == u2.getId()) {
            return true;
        }

        return isInAdminGroup(u1);
    }

    @Override
    public boolean canManageSubscriptionsForGroup(WvUser u, Group g) {
        return isInAdminGroup(u) || isGroupAdmin(u, g);
    }

    @Override
    public boolean canManageSubscriptionsForUser(WvUser u1, WvUser u2) {
        return canSeeSubscriptionsOfUser(u1, u2);
    }

    @Override
    public boolean canManageRules(WvUser u) {
        return isInAdminGroup(u);
    }

    @Override
    public boolean canSeeSeries(WvUser u, int seriesId) {
//        if (isInAdminGroup(u)) {
//            return true;
//        }

        //currently no restrictions defined
        return true;
    }

    @Override
    public boolean isInAdminGroup(WvUser u) {
        if (u.getGroups() != null) {
            if (u.getGroups().stream().anyMatch((group) -> (adminGroups.contains(group.getName())))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isGroupAdmin(WvUser u, Group g) {
        return u.getGroups().stream().filter((Group ag) -> {
            String agName = ag.getName();
            return agName.startsWith(g.getName()) && agName.endsWith(policies.getAdminSuffix());
        }).count() > 0;
    }


}
