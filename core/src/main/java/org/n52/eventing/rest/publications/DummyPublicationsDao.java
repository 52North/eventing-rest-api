
package org.n52.eventing.rest.publications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummyPublicationsDao implements PublicationsDao {

    private final Map<String, Publication> publications = new HashMap<>();

    public DummyPublicationsDao() {
        publications.put("dummy-pub", new Publication("dummy-pub", "dummy-pub ftw", "this publication provides niiiice data"));
    }


    @Override
    public synchronized boolean hasPublication(String id) {
        return publications.containsKey(id);
    }

    @Override
    public synchronized List<Publication> getPublications() {
        return Collections.unmodifiableList(new ArrayList<>(publications.values()));
    }

    @Override
    public synchronized Publication getPublication(String id) throws UnknownPublicationsException {
        if (!hasPublication(id)) {
            throw new UnknownPublicationsException("Publication does not exist: "+id);
        }

        return publications.get(id);
    }

}
