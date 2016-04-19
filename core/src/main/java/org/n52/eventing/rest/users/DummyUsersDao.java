
package org.n52.eventing.rest.users;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummyUsersDao implements UsersDao {

    private final User dummyUser = new User("dummy-user", "Peter", "Paul", "peter@paul.de");
    
    @Override
    public User getUser(String id) throws UnknownUserException {
        if (hasUser(id)) {
            return dummyUser;
        }
        
        throw new UnknownUserException("Unknown user: "+ id);
    }

    @Override
    public boolean hasUser(String id) {
        return dummyUser.getId().equals(id);
    }

}
