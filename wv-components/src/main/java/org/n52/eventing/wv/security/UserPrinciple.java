
package org.n52.eventing.wv.security;

import org.n52.eventing.wv.model.User;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UserPrinciple {

    private final User user;
    private final boolean adminRights;

    public UserPrinciple(User user, boolean adminRights) {
        this.user = user;
        this.adminRights = adminRights;
    }

    public User getUser() {
        return user;
    }

    public boolean isAdminRights() {
        return adminRights;
    }

}
