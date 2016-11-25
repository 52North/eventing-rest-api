
package org.n52.eventing.wv.dao.hibernate;

import org.hibernate.Session;
import org.n52.eventing.wv.dao.DatabaseException;
import org.n52.eventing.wv.model.Rule;
import org.n52.eventing.wv.dao.RuleDao;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class HibernateRuleDao extends BaseHibernateDao<Rule> implements RuleDao {

    public HibernateRuleDao(Session session) {
        super(session);
    }



    @Override
    public void removeRule(Rule r) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
