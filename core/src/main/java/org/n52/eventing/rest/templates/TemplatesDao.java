package org.n52.eventing.rest.templates;

import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface TemplatesDao {

    boolean hasTemplate(String id);

    Template getTemplate(String id) throws UnknownTemplateException;

    List<Template> getTemplates();

}
