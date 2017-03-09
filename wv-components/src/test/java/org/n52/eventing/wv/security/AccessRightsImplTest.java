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

import java.util.Collections;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.WvUser;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AccessRightsImplTest {

    @Test
    public void testRights() throws Exception {
        Group g1 = new Group();
        g1.setName("admins-test");

        WvUser admin1 = new WvUser("a1", "asdf", null, null, null, 1, Collections.singleton(g1));
        admin1.setId(0);
        WvUser user1 = new WvUser("u1", "asdf", null, null, null, 1, null);
        user1.setId(1);

        AccessRightsImpl ar = new AccessRightsImpl();
        ar.setPolicies(new GroupPolicies());
        ar.afterPropertiesSet();

        Assert.assertThat(ar.canManageRules(admin1), CoreMatchers.is(true));
        Assert.assertThat(ar.canManageRules(user1), CoreMatchers.is(false));

        Assert.assertThat(ar.canSeeSubscriptionsOfGroup(admin1, g1), CoreMatchers.is(true));
        Assert.assertThat(ar.canSeeSubscriptionsOfGroup(user1, g1), CoreMatchers.is(false));

        Assert.assertThat(ar.canSeeSubscriptionsOfUser(admin1, user1), CoreMatchers.is(true));
        Assert.assertThat(ar.canSeeSubscriptionsOfUser(user1, admin1), CoreMatchers.is(false));
        Assert.assertThat(ar.canSeeSubscriptionsOfUser(user1, user1), CoreMatchers.is(true));
    }

}
