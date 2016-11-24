package org.n52.eventing.wv.dao;

import java.util.List;
import java.util.Optional;
import org.n52.eventing.wv.model.Series;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface SeriesDao {
    
    Optional<Series> retrieveSeries(int id);
    
    List<Series> retrieveSeries();
    
    void storeSeries(Series s) throws DatabaseException;
    
}
