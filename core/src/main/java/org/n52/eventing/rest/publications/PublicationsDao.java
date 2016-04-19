package org.n52.eventing.rest.publications;

import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface PublicationsDao {

    boolean hasPublication(String id);

    List<Publication> getPublications();

    Publication getPublication(String id) throws UnknownPublicationsException;

}
