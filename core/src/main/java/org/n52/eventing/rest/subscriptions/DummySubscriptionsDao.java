
package org.n52.eventing.rest.subscriptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.eventing.rest.deliverymethods.DeliveryMethodsDao;
import org.n52.eventing.rest.deliverymethods.UnknownDeliveryMethodException;
import org.n52.eventing.rest.publications.PublicationsDao;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.n52.eventing.rest.templates.TemplatesDao;
import org.n52.eventing.rest.templates.UnknownTemplateException;
import org.n52.eventing.rest.users.UnknownUserException;
import org.n52.eventing.rest.users.UsersDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DummySubscriptionsDao implements SubscriptionsDao {

    private static final Logger LOG = LoggerFactory.getLogger(DummySubscriptionsDao.class);
    private static final DateTimeFormatter ISO_FORMATTER = ISODateTimeFormat.dateTime();

    private final Map<String, Subscription> subscriptions = new HashMap<>();

    private final PublicationsDao publicationsDao;
    private final UsersDao usersDao;
    private final DeliveryMethodsDao deliveryMethodsDao;
    private final TemplatesDao templatesDao;


    public DummySubscriptionsDao(UsersDao usersDao, PublicationsDao publicationsDao,
            DeliveryMethodsDao deliveryMethodsDao, TemplatesDao templatesDao) {
        this.usersDao = usersDao;
        this.publicationsDao = publicationsDao;
        this.deliveryMethodsDao = deliveryMethodsDao;
        this.templatesDao = templatesDao;
        LOG.info("initializing subscriptions...");

        try {
            Subscription sub = new Subscription("dummy-sub", "dummy-sub yeah", "this subscription is set up!");
            sub.setUser(this.usersDao.getUser("dummy-user"));
            sub.setPublicationId(this.publicationsDao.getPublication("dummy-pub").getId());
            sub.setDeliveryMethodId(this.deliveryMethodsDao.getDeliveryMethod("email").getId());
            sub.setEndOfLife(new DateTime().plusMonths(2).toString(ISO_FORMATTER));
            sub.setStatus(Subscription.Status.ENABLED);
            sub.setTemplateId(this.templatesDao.getTemplate("overshootUndershoot").getId());
            sub.setConsumer("peterchen@paulchen.de");
            subscriptions.put("dummy-sub", sub);
        } catch (UnknownPublicationsException | UnknownUserException
                | UnknownTemplateException | UnknownDeliveryMethodException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    @Override
    public synchronized boolean hasSubscription(String id) {
        return subscriptions.containsKey(id);
    }

    @Override
    public synchronized List<Subscription> getSubscriptions() {
        return Collections.unmodifiableList(new ArrayList<>(subscriptions.values()));
    }

    @Override
    public synchronized Subscription getSubscription(String id) throws UnknownSubscriptionException {
        if (!hasSubscription(id)) {
            throw new UnknownSubscriptionException("Subscription does not exist: "+id);
        }

        return subscriptions.get(id);
    }

    @Override
    public synchronized void addSubscription(String subId, Subscription subscription) {
        this.subscriptions.put(subId, subscription);
    }

}
