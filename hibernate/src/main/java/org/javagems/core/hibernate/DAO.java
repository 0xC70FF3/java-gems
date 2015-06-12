package org.javagems.core.hibernate;

import com.googlecode.genericdao.dao.hibernate.GenericDAO;
import com.googlecode.genericdao.dao.hibernate.GenericDAOImpl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;

public abstract class DAO<T, ID extends Serializable> extends GenericDAOImpl<T, ID> implements GenericDAO<T, ID> {
    final private SessionFactory sessionFactory;
    private Session session;

    public DAO() {
        this(HibernateUtils.getSessionFactory());
    }

    public DAO(SessionFactory sessionFactory) {
        this.session = null;
        this.sessionFactory = sessionFactory;
        super.setSessionFactory(this.sessionFactory);
    }

    @Override
    public Session getSession() {
        if (this.session == null) {
            this.session = super.getSessionFactory().openSession();
        }
        return this.session;
    }

    public void closeSessionQuietly() {
        if (this.session != null) {
            this.session.close();
            this.session = null;
        }
    }
}
