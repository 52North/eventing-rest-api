
package org.n52.eventing.wv.dao.hibernate;

import java.util.Optional;
import org.hibernate.Session;
import org.n52.eventing.wv.model.Category;
import org.n52.eventing.wv.dao.BaseDao;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateCategoryDao extends BaseHibernateDao<Category> implements BaseDao<Category> {

    public HibernateCategoryDao(Session s) {
        super(s);
    }

    @Override
    public Optional<Category> retrieveByName(String name) {
        return super.retrieveByKey("categoryId", name);
    }
    
    

}
