// http://notatube.blogspot.com/2010/03/hibernate-using-event-listener-to-set.html
// http://notatube.blogspot.com/2010/03/hibernate-using-event-listener-to-set.html
// http://lists.jboss.org/pipermail/hibernate-issues/2008-November/012834.html
// http://www.java2s.com/Code/Java/Hibernate/EventYourOwnSaveOrUpdateEventListener.htm
// http://docs.jboss.org/hibernate/core/3.3/reference/en/html/querycriteria.html
package org.javagems.core.hibernate;

import org.hibernate.SessionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HibernateUtils {
    public static final String DEFAULT_DATABASE_KEY = "__default";

    private static class LazyHolder {
        private static Map<String, SessionFactory> sessionFactories = new HashMap<String, SessionFactory>();
    }

    private static Map<String, SessionFactory> getInstance() {
        return LazyHolder.sessionFactories;
    }

    public static void storeSessionFactory(String key, SessionFactory sessionFactory) {
        synchronized (LazyHolder.sessionFactories) {
            if (HibernateUtils.getInstance().containsKey(key)) {
                HibernateUtils.getInstance().get(key).close();
            }
            HibernateUtils.getInstance().put(key, sessionFactory);
        }
    }

    public static void clear() {
        synchronized (LazyHolder.sessionFactories) {
            for (String key : HibernateUtils.getInstance().keySet()) {
                HibernateUtils.getInstance().get(key).close();
            }
            HibernateUtils.getInstance().clear();
        }
    }

    public static SessionFactory getSessionFactory() {
        SessionFactory sessionFactory = HibernateUtils.getSessionFactory(DEFAULT_DATABASE_KEY);
        if (sessionFactory == null) {
            synchronized (LazyHolder.sessionFactories) {
                if (!HibernateUtils.getInstance().containsKey(DEFAULT_DATABASE_KEY)) {
                    sessionFactory = new SessionFactoryBuilder().build();
                    HibernateUtils.storeSessionFactory(DEFAULT_DATABASE_KEY, sessionFactory);
                }
            }
        }
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory(String key) {
        return HibernateUtils.getInstance().get(key);
    }

    public static Set<String> keySet() {
        return HibernateUtils.getInstance().keySet();
    }
}
