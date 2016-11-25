
package org.n52.eventing.wv.dao.hibernate;

import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.dao.DatabaseException;
import org.springframework.core.GenericTypeResolver;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 * @param <T> the model of this Dao
 */
public class BaseHibernateDao<T> {

    private final Class<T> genericType;
    private final Session session;

    public BaseHibernateDao(Session session) {
        this.session = session;
        this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), BaseHibernateDao.class);
    }

    public Optional<T> retrieveById(int id) {
        T retrieved = session.get(this.genericType, id);
        retrieved = initializeProxies(retrieved);
        return Optional.ofNullable(retrieved);
    }

    public List<T> retrieve(Pagination pagination) throws DatabaseException {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(this.genericType);
        Query<T> query = session.createQuery(criteria);
        query.setFirstResult(pagination != null ? pagination.getOffset() : 0);
        query.setMaxResults(pagination != null ? pagination.getSize() : 100);
        return query.list();
    };

    public boolean exists(int id) {
        T retrieved = session.get(this.genericType, id);
        return retrieved != null;
    }

    public boolean exists(String name) {
        return retrieveByName(name).isPresent();
    }

    public Optional<T> retrieveByName(String name) {
        return retrieveByKey("name", name);
    }

    protected Optional<T> retrieveByKey(String key, String value) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.genericType);
        Root<T> root = query.from(this.genericType);
        query.where(builder.equal(root.get(key), value));
        List<T> result = getSession().createQuery(query).list();
        return Optional.ofNullable(result.isEmpty() ? null : result.get(0));
    }

    public void store(T o) throws DatabaseException {
        session.persist(o);
    }

    public void remove(T r) throws DatabaseException {
        session.delete(r);
    }

    protected T initializeProxies(T o) {
        Hibernate.initialize(o);
        return o;
    }

    protected Session getSession() {
        return session;
    }

}
