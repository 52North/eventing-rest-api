package org.n52.eventing.wv.dao;

import java.util.List;
import java.util.Optional;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.model.Series;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface SeriesDao {

    Optional<Series> retrieveById(int id);

    List<Series> retrieve(Pagination pagination) throws DatabaseException;

    void store(Series s) throws DatabaseException;

}
