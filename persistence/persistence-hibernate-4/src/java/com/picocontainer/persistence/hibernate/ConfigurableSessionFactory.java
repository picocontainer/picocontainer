/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                     *
 *****************************************************************************/

package com.picocontainer.persistence.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.stat.Statistics;

import com.picocontainer.Disposable;
import com.picocontainer.PicoCompositionException;

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
    
    private final ServiceRegistry serviceRegistry;

    public ConfigurableSessionFactory(Configuration configuration) {
        try {
        	
        	ServiceRegistryBuilder registryBuilder = new ServiceRegistryBuilder();
        	registryBuilder.applySettings(configuration.getProperties());
        	serviceRegistry = registryBuilder.buildServiceRegistry();
            delegate = configuration.buildSessionFactory(serviceRegistry);
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
    @Deprecated
	@SuppressWarnings("rawtypes")
	public void evict(Class persistentClass) {
        delegate.evict(persistentClass);
    }

	/** {@inheritDoc} **/
    @Deprecated
	@SuppressWarnings("rawtypes")
	public void evict(Class persistentClass, Serializable id) {
        delegate.evict(persistentClass, id);
    }

	/** {@inheritDoc} **/
    @Deprecated
    public void evictCollection(String roleName) {
        delegate.evictCollection(roleName);
    }

	/** {@inheritDoc} **/
    @Deprecated
    public void evictCollection(String roleName, Serializable id) {
        delegate.evictCollection(roleName, id);
    }

	/** {@inheritDoc} **/
    @Deprecated
    public void evictEntity(String entityName) {
        delegate.evictEntity(entityName);
    }

	/** {@inheritDoc} **/
    @Deprecated
    public void evictEntity(String entityName, Serializable id) {
        delegate.evictEntity(entityName, id);
    }

	/** {@inheritDoc} **/
    @Deprecated
    public void evictQueries() {
        delegate.evictQueries();
    }

	/** {@inheritDoc} **/
    @Deprecated
    public void evictQueries(String cacheRegion) {
        delegate.evictQueries(cacheRegion);
    }

	/** {@inheritDoc} **/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getAllClassMetadata() {
        return delegate.getAllClassMetadata();
    }

	/** {@inheritDoc} **/
	@SuppressWarnings("rawtypes")
	public Map getAllCollectionMetadata() {
        return delegate.getAllCollectionMetadata();
    }

	/** {@inheritDoc} **/
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings("rawtypes")
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

	public Cache getCache() {
		return delegate.getCache();
	}

	public boolean containsFetchProfileDefinition(String name) {
		return delegate.containsFetchProfileDefinition(name);
	}

	public TypeHelper getTypeHelper() {
		return delegate.getTypeHelper();
	}

	public SessionFactoryOptions getSessionFactoryOptions() {
		return delegate.getSessionFactoryOptions();
	}

	public SessionBuilder withOptions() {
		return delegate.withOptions();
	}

	public Session openSession() throws HibernateException {
		return delegate.openSession();
	}

	public Session getCurrentSession() throws HibernateException {
		return delegate.getCurrentSession();
	}

	public StatelessSessionBuilder withStatelessOptions() {
		return delegate.withStatelessOptions();
	}



}
