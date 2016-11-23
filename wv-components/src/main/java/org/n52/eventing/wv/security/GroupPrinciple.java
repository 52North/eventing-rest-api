
package org.n52.eventing.wv.security;

import org.n52.eventing.wv.model.Group;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class GroupPrinciple implements GrantedAuthority {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String USER_ROLE = "ROLE_USER";
    
    private final Group group;

    public GroupPrinciple(Group group) {
        this.group = group;
    }

    @Override
    public String getAuthority() {
        if (group != null && "admin".equals(group.getName())) {
            return ADMIN_ROLE;
        }
        
        return USER_ROLE;
    }
    
}
