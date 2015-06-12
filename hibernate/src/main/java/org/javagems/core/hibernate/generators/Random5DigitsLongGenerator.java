package org.javagems.core.hibernate.generators;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class Random5DigitsLongGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object parent) throws HibernateException {
        return new Long((long) (Math.random() * 90000 + 10000));
    }
}
