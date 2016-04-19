package org.n52.eventing.rest.users;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface UsersDao {

    User getUser(String id) throws UnknownUserException;

    boolean hasUser(String id);

}
