/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.persistence.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.picocontainer.Disposable;
import org.picocontainer.PicoCompositionException;

/**
 * Session factory implementation that uses a delegate session factory 
 * created from configuration.
 * 
 * @author Jose Peleteiro
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public final class ConfigurableSessionFactory implements SessionFactory, Disposable {

    private final SessionFactory delegate;

    public ConfigurableSessionFactory(Configuration configuration) {
        try {
            delegate = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new PicoCompositionException(e);
        }
    }

    public SessionFactory getDelegate() {
        return delegate;
    }

	/** {@inheritDoc} **/
    public void close() {
        delegate.close();
    }

	/** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public void evict(Class persistentClass) {
        delegate.evict(persistentClass);
    }

	/** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public void evict(Class persistentClass, Serializable id) {
        delegate.evict(persistentClass, id);
    }

	/** {@inheritDoc} **/
    public void evictCollection(String roleName) {
        delegate.evictCollection(roleName);
    }

	/** {@inheritDoc} **/
    public void evictCollection(String roleName, Serializable id) {
        delegate.evictCollection(roleName, id);
    }

	/** {@inheritDoc} **/
    public void evictEntity(String entityName) {
        delegate.evictEntity(entityName);
    }

	/** {@inheritDoc} **/
    public void evictEntity(String entityName, Serializable id) {
        delegate.evictEntity(entityName, id);
    }

	/** {@inheritDoc} **/
    public void evictQueries() {
        delegate.evictQueries();
    }

	/** {@inheritDoc} **/
    public void evictQueries(String cacheRegion) {
        delegate.evictQueries(cacheRegion);
    }

	/** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public Map getAllClassMetadata() {
        return delegate.getAllClassMetadata();
    }

	/** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public Map getAllCollectionMetadata() {
        return delegate.getAllCollectionMetadata();
    }

	/** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public ClassMetadata getClassMetadata(Class persistentClass) {
        return delegate.getClassMetadata(persistentClass);
    }

	/** {@inheritDoc} **/
    public ClassMetadata getClassMetadata(String entityName) {
        return delegate.getClassMetadata(entityName);
    }

	/** {@inheritDoc} **/
    public CollectionMetadata getCollectionMetadata(String roleName) {
        return delegate.getCollectionMetadata(roleName);
    }

	/** {@inheritDoc} **/
	public Session getCurrentSession() {
		return delegate.getCurrentSession();
	}

	/** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public Set getDefinedFilterNames() {
        return delegate.getDefinedFilterNames();
    }

	/** {@inheritDoc} **/
   public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return delegate.getFilterDefinition(filterName);
    }

	/** {@inheritDoc} **/
    public Reference getReference() throws NamingException {
        return delegate.getReference();
    }

	/** {@inheritDoc} **/
    public Statistics getStatistics() {
        return delegate.getStatistics();
    }

	/** {@inheritDoc} **/
	public boolean isClosed() {
		return delegate.isClosed();
	}

	/** {@inheritDoc} **/
    public Session openSession() {
        return delegate.openSession();
    }

	/** {@inheritDoc} **/
    public Session openSession(Connection connection) {
        return delegate.openSession(connection);
    }

	/** {@inheritDoc} **/
    public Session openSession(Connection connection, Interceptor interceptor) {
        return delegate.openSession(connection, interceptor);
    }

	/** {@inheritDoc} **/
    public Session openSession(Interceptor interceptor) {
        return delegate.openSession(interceptor);
    }

	/** {@inheritDoc} **/
    public StatelessSession openStatelessSession() {
        return delegate.openStatelessSession();
    }

	/** {@inheritDoc} **/
    public StatelessSession openStatelessSession(Connection connection) {
        return delegate.openStatelessSession(connection);
    }

    /**
     * Clears the session factory when the container is disposed.
     */
	public void dispose() {
		close();
	}

}
