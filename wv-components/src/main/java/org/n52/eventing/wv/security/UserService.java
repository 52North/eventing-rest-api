
package org.n52.eventing.wv.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.dao.UserGroupsDao;
import org.n52.eventing.wv.model.Group;
import org.n52.eventing.wv.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @since 4.0.0
 * 
 */
public class UserService implements AuthenticationProvider, Serializable {
    private static final long serialVersionUID = -3207103212342510378L;

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserGroupsDao dao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UsernamePasswordAuthenticationToken authenticate(Authentication authentication)
            throws AuthenticationException {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
        User user = authenticate((String) auth.getPrincipal(), (String) auth.getCredentials());
        return new UsernamePasswordAuthenticationToken(new UserPrinciple(user,
                containsAdminGroup(user.getGroups())), null, createPrincipals(user.getGroups()));
    }

    public User authenticate(final String username, final String password) throws AuthenticationException {
        if (username == null || password == null) {
            throw new BadCredentialsException("Bad Credentials");
        }
        
        Optional<User> user;
        try {
            user = dao.retrieveUserByName(username);
        } catch (DatabaseException ex) {
            LOG.warn("Could not retrieve user: {}", ex.getMessage());
            throw new AuthenticationServiceException(ex.getMessage(), ex);
        }
        

        if (user == null || !user.isPresent()) {
            throw new BadCredentialsException("Bad Credentials");
        }

        if (!getPasswordEncoder().matches(password, user.get().getPassword())) {
            throw new BadCredentialsException("Bad Credentials");
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


    private boolean containsAdminGroup(Set<Group> groups) {
        if (groups == null) {
            return false;
        }
        
        return groups.stream().filter((Group g) -> {
            return "admin".equals(g.getName());
        }).count() > 0;
    }

    private Collection<? extends GrantedAuthority> createPrincipals(Set<Group> groups) {
        if (groups == null) {
            return Collections.emptyList();
        }
        return groups.stream().map(new Function<Group, GroupPrinciple>() {
            @Override
            public GroupPrinciple apply(Group t) {
                return new GroupPrinciple(t);
            }
            
        }).collect(Collectors.toList());
    }

    public void setDao(UserGroupsDao dao) {
        this.dao = dao;
    }
}
