package org.n52.eventing.wv.dao;

import java.util.List;
import java.util.Optional;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.wv.model.Rule;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface RuleDao {


    Optional<Rule> retrieveById(int id);

    List<Rule> retrieve(Pagination pagination) throws DatabaseException;

    void store(Rule r) throws DatabaseException;

    void removeRule(Rule r) throws DatabaseException;

}
