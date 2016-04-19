
package org.n52.eventing.rest.subscriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsDao;
import org.n52.eventing.rest.publications.PublicationsDao;
import org.n52.eventing.rest.subscriptions.Subscription.Status;
import org.n52.eventing.rest.templates.Parameter;
import org.n52.eventing.rest.templates.Template;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.rest.users.UnknownUserException;
import org.n52.eventing.rest.users.User;
import org.n52.eventing.rest.users.UsersDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionManagerImpl implements SubscriptionManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionManagerImpl.class);
    
    @Autowired
    private SubscriptionsDao dao;
    
    @Autowired
    private PublicationsDao publicationsDao;
    
    @Autowired
    private UsersDao usersDao;
    
    @Autowired
    private DeliveryMethodsDao deliveryMethodsDao;
    
    @Autowired
    private TemplatesDao templatesDao;
    
    
    @Override
    public String subscribe(SubscriptionDefinition subDef) throws InvalidSubscriptionException {
        Objects.requireNonNull(subDef.getPublicationId());
        Objects.requireNonNull(subDef.getTemplateId());
        Objects.requireNonNull(subDef.getConsumer());
        Objects.requireNonNull(subDef.getDeliveryMethodId());
        
        //TODO implement using Spring security
        User user;
        try {
            user = this.usersDao.getUser("dummy-user");
        } catch (UnknownUserException ex) {
            throw new InvalidSubscriptionException(ex.getMessage(), ex);
        }
        
        String pubId = subDef.getPublicationId();
        if (!this.publicationsDao.hasPublication(pubId)) {
            throw new InvalidSubscriptionException("Publication unknown: "+pubId);
        }
        
        String templateId = subDef.getTemplateId();
        if (!this.templatesDao.hasTemplate(templateId)) {
            throw new InvalidSubscriptionException("Template unknown: "+pubId);
        }
        
        String deliveryMethodId = subDef.getDeliveryMethodId();
        if (!this.deliveryMethodsDao.hasDeliveryMethod(deliveryMethodId)) {
            throw new InvalidSubscriptionException("DeliveryMethod unknown: "+deliveryMethodId);
        }
        
        String consumer = subDef.getConsumer();
        
        String subId = UUID.randomUUID().toString();
        
        String desc = String.format("Subscription using template %s (created: %s)", templateId, new DateTime());
        String label = Optional.ofNullable(subDef.getLabel()).orElse(desc);
        
        Subscription subscription = new Subscription(subId, label,
                desc);
        
        subscription.setConsumer(consumer);
        subscription.setTemplateId(templateId);
        subscription.setDeliveryMethodId(deliveryMethodId);
        subscription.setPublicationId(pubId);
        subscription.setUser(user);
        subscription.setParameters(resolveAndCreateParameters(subDef.getParameters(),
                templateId));
        subscription.setStatus(resolveStatus(subDef.getStatus()));
        
        this.dao.addSubscription(subId, subscription);
        
                String eol = subDef.getEndOfLife();
        if (eol != null && !eol.isEmpty()) {
            try {
                this.dao.updateEndOfLife(subId, parseEndOfLife(eol));
            } catch (UnknownSubscriptionException ex) {
                throw new InvalidSubscriptionException(ex.getMessage(), ex);
            }
        }
        
        return subId;
    }
    
    private Status resolveStatus(String status) throws InvalidSubscriptionException {
        if (status == null) {
            return Status.ENABLED;
        }
        
        for (Status value : Status.values()) {
            if (status.equalsIgnoreCase(value.name())) {
                return value;
            }
        }
        
        throw new InvalidSubscriptionException("Invalid status provided: "+status);
    }
    
    private List<ParameterValue> resolveAndCreateParameters(List<Map<String, Object>> parameters, String templateId)
            throws InvalidSubscriptionException {
        Template template;
        try {
            template = this.templatesDao.getTemplate(templateId);
        } catch (UnknownTemplateException ex) {
            throw new InvalidSubscriptionException("Template not available: "+ templateId, ex);
        }
        
        final List<Parameter> templateParameters = template.getParameters();
        
        try {
            return parameters.stream().map((Map<String, Object> t) -> {
                for (String key : t.keySet()) {
                    Parameter templateParameter = resolveTemplateParameter(templateParameters, key);
                    return new ParameterValue(templateParameter.getName(), t.get(key), templateParameter.getDataType());
                }

                throw new RuntimeException(new InvalidSubscriptionException("No parameter values available"));
            }).collect(Collectors.toCollection(ArrayList::new));
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof InvalidSubscriptionException) {
                throw (InvalidSubscriptionException) e.getCause();
            }
            throw new InvalidSubscriptionException("Could not resolve parameters", e);
        }
    }
    
    private Parameter resolveTemplateParameter(List<Parameter> templateParameters, String key) {
        Optional<Parameter> match = templateParameters.stream().filter((Parameter p) -> {
            return p.getName().equals(key);
        }).findFirst();
        
        if (match.isPresent()) {
            return match.get();
        }
        
        throw new RuntimeException(new InvalidSubscriptionException("Invalid template parameter: "+key));
    }

    @Override
    public void updateSubscription(SubscriptionUpdateDefinition subDef) throws InvalidSubscriptionException {
        String eolString = subDef.getEndOfLife();
        if (eolString != null && !eolString.isEmpty()) {
            DateTime eol = parseEndOfLife(eolString);
            try {
                this.dao.updateEndOfLife(subDef.getId(), eol);
            } catch (UnknownSubscriptionException ex) {
                throw new InvalidSubscriptionException(ex.getMessage(), ex);
            }
            
            changeEndOfLife(subDef.getId(), eol);
        }
        
        String statusString = subDef.getStatus();
        if (statusString != null && !statusString.isEmpty()) {
            Status status = resolveStatus(statusString);

            try {
                this.dao.updateStatus(subDef.getId(), status);
            } catch (UnknownSubscriptionException ex) {
                throw new InvalidSubscriptionException(ex.getMessage(), ex);
            }

            switch (status) {
                case ENABLED:
                    resume(subDef.getId());
                    break;
                case DISABLED:
                    pause(subDef.getId());
                    break;
                default:
                    break;
            }
        }
    }

    private DateTime parseEndOfLife(String eolString) throws InvalidSubscriptionException {
        try {
            return new DateTime(eolString);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidSubscriptionException("Not a valid xs:date: "+eolString);
        }
    }

    private void resume(String id) {
        LOG.debug("TODO: Implement resume");
    }

    private void pause(String id) {
        LOG.debug("TODO: Implement pause");
    }

    private void changeEndOfLife(String id, DateTime eol) {
        LOG.debug("TODO: Implement end of life update");
    }
}
