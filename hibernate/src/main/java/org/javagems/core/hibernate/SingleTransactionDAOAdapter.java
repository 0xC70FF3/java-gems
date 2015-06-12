package org.javagems.core.hibernate;

import com.googlecode.genericdao.search.ISearch;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class SingleTransactionDAOAdapter<T, ID extends Serializable> extends DAO<T, ID> {

    private static final int BULK_SIZE = 2048;

    public SingleTransactionDAOAdapter(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    protected void closeQuietly(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }

    @Override
    public T find(Serializable id) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            T result = super.find(id);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }

    @Override
    public List<T> findAll() {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            List<T> result = super.findAll();
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }

    @Override
    public boolean save(T user) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            boolean result = super.save(user);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }
    
    @Override
    public boolean[] save(T... entities) {
        Transaction transaction = null;
        try {
            boolean[] result = new boolean[entities.length];
            if (entities.length > 0) {
                transaction = this.getSession().beginTransaction();
                int bulkSize = BULK_SIZE;
                int beginIndex = 0;
                int endIndex = Math.min(bulkSize, entities.length);                
                while(beginIndex < endIndex) {                    
                    T[] slice = Arrays.copyOfRange(entities, beginIndex, endIndex);
                    System.arraycopy(super.save(slice), 0, result, beginIndex, slice.length);
                    super.flush(); 
                    beginIndex = endIndex;
                    endIndex = Math.min(endIndex + bulkSize, entities.length);
                }
                transaction.commit();
            }
            return result;
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }
    
    @Override
    public <RT> RT searchUnique(ISearch search) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            RT result = super.searchUnique(search);
            transaction.commit();
            return result;
        } finally {
            this.closeSessionQuietly();
        }
    }

    @Override
    public <RT> List<RT> search(ISearch search) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            List<RT> result = super.search(search);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }
    
    public void remove(ISearch search) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            List<T> result = super.<T>search(search);
            if (result.size() > 0) {
                @SuppressWarnings("unchecked")
                T[] array = (T[])Array.newInstance(result.get(0).getClass(), 0);
                super.remove(result.toArray(array));
            }
            transaction.commit();            
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }   
    
    @Override
    public boolean removeById(Serializable id) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            boolean result = super.removeById(id);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }
    
    @Override
    public void removeByIds(Serializable... ids) {
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            super.removeByIds(ids);
            super.flush(); // needed for bulks
            transaction.commit();            
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
    }
    
    
    
    @Override
    public boolean remove(T entity) {
        boolean result = false;
        Transaction transaction = null;
        try {
            transaction = this.getSession().beginTransaction();
            result = super.remove(entity);
            super.flush(); // needed for bulks
            transaction.commit();            
        } catch (RuntimeException ex) {
            this.closeQuietly(transaction);
            throw ex;
        } finally {
            this.closeSessionQuietly();
        }
        
        return result;
    }
}
