package org.n52.eventing.wv.dao;

import java.util.List;
import java.util.Optional;
import org.n52.eventing.rest.Pagination;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface BaseDao<T> {

    Optional<T> retrieveById(int id);

    List<T> retrieve(Pagination pagination) throws DatabaseException;

    void store(T r) throws DatabaseException;

    void remove(T r) throws DatabaseException;

    boolean exists(String name);

    Optional<T> retrieveByName(String name);

}
