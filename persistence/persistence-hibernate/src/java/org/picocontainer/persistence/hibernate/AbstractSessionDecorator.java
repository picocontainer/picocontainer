/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * license.txt file.
 ******************************************************************************/

package org.picocontainer.persistence.hibernate;

import java.io.Serializable;
import java.sql.Connection;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.stat.SessionStatistics;

/**
 * Abstract session decorator
 */
public abstract class AbstractSessionDecorator implements Session {

    /**
     * Returns the Session delegate
     * 
     * @return The Session
     */
    public abstract Session getDelegate();

    /**
     * Perform actions to dispose &quot;burned&quot; session properly.
     */
    public abstract void invalidateDelegate();

    /**
     * Invalidates the session calling {@link #invalidateDelegate()} and just
     * return the <code>cause</code> back.
     * 
     * @return
     * @param cause
     */
    protected RuntimeException handleException(final RuntimeException cause) {
        try {
            invalidateDelegate();
        } catch (RuntimeException e) {
            return e;
        }
        return cause;
    }

    /** {@inheritDoc} * */
    public Transaction beginTransaction() {
        try {
            return getDelegate().beginTransaction();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void cancelQuery() {
        try {
            getDelegate().cancelQuery();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void clear() {
        try {
            getDelegate().clear();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Connection close() {
        try {
            return getDelegate().close();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @Deprecated
    public Connection connection() {
        try {
            return getDelegate().connection();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public boolean contains(final Object object) {
        try {
            return getDelegate().contains(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @SuppressWarnings("unchecked")
    public Criteria createCriteria(final Class persistentClass) {
        try {
            return getDelegate().createCriteria(persistentClass);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @SuppressWarnings("unchecked")
    public Criteria createCriteria(final Class persistentClass, final String alias) {
        try {
            return getDelegate().createCriteria(persistentClass, alias);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Criteria createCriteria(final String entityName) {
        try {
            return getDelegate().createCriteria(entityName);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Criteria createCriteria(final String entityName, final String alias) {
        try {
            return getDelegate().createCriteria(entityName, alias);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Query createFilter(final Object collection, final String queryString) {
        try {
            return getDelegate().createFilter(collection, queryString);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Query createQuery(final String queryString) {
        try {
            return getDelegate().createQuery(queryString);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public SQLQuery createSQLQuery(final String queryString) {
        try {
            return getDelegate().createSQLQuery(queryString);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void delete(final Object object) {
        try {
            getDelegate().delete(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void delete(final String entityName, final Object object) {
        try {
            getDelegate().delete(entityName, object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void disableFilter(final String filterName) {
        try {
            getDelegate().disableFilter(filterName);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Connection disconnect() {
        try {
            return getDelegate().disconnect();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Filter enableFilter(final String filterName) {
        try {
            return getDelegate().enableFilter(filterName);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void evict(final Object object) {
        try {
            getDelegate().evict(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void flush() {
        try {
            getDelegate().flush();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @SuppressWarnings("unchecked")
    public Object get(final Class clazz, final Serializable id) {
        try {
            return getDelegate().get(clazz, id);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @SuppressWarnings("unchecked")
    public Object get(final Class clazz, final Serializable id, final LockMode lockMode) {
        try {
            return getDelegate().get(clazz, id, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Object get(final String entityName, final Serializable id) {
        try {
            return getDelegate().get(entityName, id);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Object get(final String entityName, final Serializable id, final LockMode lockMode) {
        try {
            return getDelegate().get(entityName, id, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public CacheMode getCacheMode() {
        try {
            return getDelegate().getCacheMode();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public LockMode getCurrentLockMode(final Object object) {
        try {
            return getDelegate().getCurrentLockMode(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Filter getEnabledFilter(final String filterName) {
        try {
            return getDelegate().getEnabledFilter(filterName);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public EntityMode getEntityMode() {
        try {
            return getDelegate().getEntityMode();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public String getEntityName(final Object object) {
        try {
            return getDelegate().getEntityName(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public FlushMode getFlushMode() {
        try {
            return getDelegate().getFlushMode();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Serializable getIdentifier(final Object object) {
        try {
            return getDelegate().getIdentifier(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Query getNamedQuery(final String queryName) {
        try {
            return getDelegate().getNamedQuery(queryName);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Session getSession(final EntityMode entityMode) {
        try {
            return getDelegate().getSession(entityMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public SessionFactory getSessionFactory() {
        try {
            return getDelegate().getSessionFactory();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public SessionStatistics getStatistics() {
        try {
            return getDelegate().getStatistics();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Transaction getTransaction() {
        try {
            return getDelegate().getTransaction();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public boolean isConnected() {
        try {
            return getDelegate().isConnected();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public boolean isDirty() {
        try {
            return getDelegate().isDirty();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public boolean isOpen() {
        try {
            return getDelegate().isOpen();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @SuppressWarnings("unchecked")
    public Object load(final Class theClass, final Serializable id) {
        try {
            return getDelegate().load(theClass, id);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @SuppressWarnings("unchecked")
    public Object load(final Class theClass, final Serializable id, final LockMode lockMode) {
        try {
            return getDelegate().load(theClass, id, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void load(final Object object, final Serializable id) {
        try {
            getDelegate().load(object, id);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Object load(final String entityName, final Serializable id) {
        try {
            return getDelegate().load(entityName, id);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Object load(final String entityName, final Serializable id, final LockMode lockMode) {
        try {
            return getDelegate().load(entityName, id, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void lock(final Object object, final LockMode lockMode) {
        try {
            getDelegate().lock(object, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void lock(final String entityEntity, final Object object, final LockMode lockMode) {
        try {
            getDelegate().lock(entityEntity, object, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Object merge(final Object object) {
        try {
            return getDelegate().merge(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Object merge(final String entityName, final Object object) {
        try {
            return getDelegate().merge(entityName, object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void persist(final Object object) {
        try {
            getDelegate().persist(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void persist(final String entityName, final Object object) {
        try {
            getDelegate().persist(entityName, object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    @Deprecated
    public void reconnect() {
        try {
            getDelegate().reconnect();
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void reconnect(final Connection conn) {
        try {
            getDelegate().reconnect(conn);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void refresh(final Object object) {
        try {
            getDelegate().refresh(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void refresh(final Object object, final LockMode lockMode) {
        try {
            getDelegate().refresh(object, lockMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void replicate(final Object object, final ReplicationMode replicationMode) {
        try {
            getDelegate().replicate(object, replicationMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void replicate(final String entityName, final Object object, final ReplicationMode replicationMode) {
        try {
            getDelegate().replicate(entityName, object, replicationMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Serializable save(final Object object) {
        try {
            return getDelegate().save(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public Serializable save(final String entityName, final Object object) {
        try {
            return getDelegate().save(entityName, object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void saveOrUpdate(final Object object) {
        try {
            getDelegate().saveOrUpdate(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void saveOrUpdate(final String entityName, final Object object) {
        try {
            getDelegate().saveOrUpdate(entityName, object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void setCacheMode(final CacheMode cacheMode) {
        try {
            getDelegate().setCacheMode(cacheMode);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void setReadOnly(final Object entity, final boolean readOnly) {
        try {
            getDelegate().setReadOnly(entity, readOnly);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void setFlushMode(final FlushMode value) {
        try {
            getDelegate().setFlushMode(value);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void update(final Object object) {
        try {
            getDelegate().update(object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

    /** {@inheritDoc} * */
    public void update(final String entityName, final Object object) {
        try {
            getDelegate().update(entityName, object);
        } catch (HibernateException ex) {
            throw handleException(ex);
        }
    }

}
