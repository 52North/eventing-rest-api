
package org.n52.eventing.wv.dao.hibernate;

import java.util.Optional;
import org.hibernate.Session;
import org.n52.eventing.wv.dao.BaseDao;
import org.n52.eventing.wv.model.Procedure;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateProcedureDao extends BaseHibernateDao<Procedure> implements BaseDao<Procedure> {

    public HibernateProcedureDao(Session s) {
        super(s);
    }

    @Override
    public Optional<Procedure> retrieveByName(String name) {
        return super.retrieveByKey("procedureId", name);
    }
    
    

}
