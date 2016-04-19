
package org.n52.eventing.rest.templates;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummyTemplatesDao implements TemplatesDao {

    private final Template dummyTemplate;

    public DummyTemplatesDao() {
        dummyTemplate = new Template("overshootUndershoot", "Overshoot/Undershoot", "Classic over-/undershoot rule",
            new Definition("<filter>waterGauge >= ${waterGaugeThreshold} --> waterGauge < ${waterGaugeThreshold}</filter>", "application/xml"));
        dummyTemplate.addParameter("waterGaugeThreshold", "number");
    }

    @Override
    public boolean hasTemplate(String id) {
        return dummyTemplate.getId().equals(id);
    }

    @Override
    public Template getTemplate(String id) throws UnknownTemplateException {
        if (hasTemplate(id)) {
            return dummyTemplate;
        }

        throw new UnknownTemplateException("not there: "+ id);
    }

    @Override
    public List<Template> getTemplates() {
        return Collections.singletonList(dummyTemplate);
    }

}
