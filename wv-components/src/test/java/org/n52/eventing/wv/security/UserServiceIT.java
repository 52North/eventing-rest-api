
package org.n52.eventing.wv.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.HibernateUserGroupsDao;
import org.n52.eventing.wv.dao.ImmutableException;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class UserServiceIT {

    private HibernateUserGroupsDao userGroupDao;
    private UserService userService;
    private BCryptPasswordEncoder encoder;

    @Before
    public void setup() throws Exception {
        HibernateDatabaseConnection hdc = new HibernateDatabaseConnection();
        hdc.afterPropertiesSet();

        this.userGroupDao = new HibernateUserGroupsDao();
        this.userGroupDao.setConnection(hdc);
        
        this.encoder = new BCryptPasswordEncoder();
        
        this.userService = new UserService();
        this.userService.setPasswordEncoder(this.encoder);
        this.userService.setDao(userGroupDao);
    }
    
    @Test
    public void testPrincipals() throws DatabaseException, ImmutableException {
        User u = new User();
        String password = "asdf";
        u.setName(UUID.randomUUID().toString().substring(0, 8));
        u.setPassword(encoder.encode(password));
        u.setGroups(Collections.singleton(new Group("admin", "admin users", true)));
        
        if (!userGroupDao.retrieveGroupByName(u.getName()).isPresent()) {
            userGroupDao.storeUser(u);
        }
        
        UsernamePasswordAuthenticationToken result = this.userService.authenticate(
                new UsernamePasswordAuthenticationToken(u.getName(), password));
        
        Assert.assertThat(result, CoreMatchers.notNullValue());
        Assert.assertThat(result.getPrincipal(), CoreMatchers.instanceOf(UserPrinciple.class));
        Assert.assertThat(((UserPrinciple) result.getPrincipal()).getUser().getName(), CoreMatchers.equalTo(u.getName()));
        
        Collection<GrantedAuthority> auths = result.getAuthorities();
        auths.forEach(ga -> Assert.assertThat(ga, CoreMatchers.instanceOf(GroupPrinciple.class)));
        
        long adminCount = auths.stream().filter((GrantedAuthority ga) -> {
            return ((GroupPrinciple) ga).getAuthority().equals(GroupPrinciple.ADMIN_ROLE);
        }).count();
        
        Assert.assertThat(adminCount, CoreMatchers.is(1L));
    }
    
}
