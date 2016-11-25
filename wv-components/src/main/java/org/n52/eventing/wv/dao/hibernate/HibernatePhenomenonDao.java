
package org.n52.eventing.wv.dao.hibernate;

import java.util.Optional;
import org.hibernate.Session;
import org.n52.eventing.wv.dao.BaseDao;
import org.n52.eventing.wv.model.Category;
import org.n52.eventing.wv.model.Phenomenon;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernatePhenomenonDao extends BaseHibernateDao<Phenomenon> implements BaseDao<Phenomenon> {

    public HibernatePhenomenonDao(Session session) {
        super(session);
    }

    @Override
    public Optional<Phenomenon> retrieveByName(String name) {
        return super.retrieveByKey("phenomenonId", name);
    }
}
