
package org.n52.eventing.wv.dao;

import javax.persistence.PersistenceException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.eventing.wv.database.HibernateDatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class BaseHibernateDao {
    
    private HibernateDatabaseConnection connection;
    
    @Autowired
    public void setConnection(HibernateDatabaseConnection hdc) {
        this.connection = hdc;
    }

    protected HibernateDatabaseConnection getConnection() {
        return connection;
    }
    
    protected void internalPersist(Object o) throws DatabaseException {
        try (Session session = connection.createSession()) {
            Transaction t = session.beginTransaction();
            
            session.persist(o);
            
            try {
                t.commit();
            }
            catch (PersistenceException e) {
                throw new DatabaseException("Could not store object", e);
            }
        }
    }
    
}
