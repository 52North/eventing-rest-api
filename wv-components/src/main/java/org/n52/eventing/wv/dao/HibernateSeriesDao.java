
package org.n52.eventing.wv.dao;

import java.util.List;
import java.util.Optional;
import org.n52.eventing.wv.model.Series;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateSeriesDao extends BaseHibernateDao implements SeriesDao {

    @Override
    public Optional<Series> retrieveSeries(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Series> retrieveSeries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void storeSeries(Series s) throws DatabaseException {
        internalPersist(s);
    }

}
