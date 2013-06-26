/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * license.txt file.
 ******************************************************************************/

package com.picocontainer.persistence.hibernate;

import java.io.Serializable;
import java.sql.Connection;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.LobHelper;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.jdbc.Work;

import com.picocontainer.Disposable;
import com.picocontainer.Startable;

/**
 * <p>
 * Session implementation which allows request scoping while supporting
 * lifecycle events. If you register this component within either a request
 * PicoContainer or a request-scoped storage object, then this component will
 * close sessions whenever stop() is called by the container, and is reusable
 * until dispose() is called.
 * </p>
 * <p>
 * This allows for the &quot;One Session Per Request&quot; pattern often used in
 * Hibernate.
 * <p>
 * 
 * @author Jose Peleteiro
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public final class ScopedSession extends AbstractSessionDecorator implements Startable, Disposable {

    /**
     * Session factory that creates the delegate sessions.
     */
    private final SessionFactory factory;

    /**
     * Current delegate session, lazily created.
     */
    private Session session = null;

    /**
     * Session-specific interceptor.
     */
    private Interceptor interceptor = null;

    /**
     * Flag indicating object should not be used again.
     */
    private boolean disposed = false;

    /**
     * Creates a ScopedSession with factory and <code>null</code> interceptor
     * 
     * @param factory session factory to create the session
     */
    public ScopedSession(final SessionFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates a ScopedSession with factory and interceptor
     * 
     * @param factory session factory to create the session
     * @param interceptor interceptor to use with created session
     */
    public ScopedSession(final SessionFactory factory, final Interceptor interceptor) {
        this(factory);
        setInterceptor(interceptor);
    }

    /** {@inheritDoc} */
    @Override
    public SessionFactory getSessionFactory() {
        return factory;
    }

    /**
     * Obtain hibernate session in lazy way.
     */
    @Override
    public Session getDelegate() {
        if (disposed) {
            throw new IllegalStateException("Component has already been disposed by parent container.");
        }

        if (session == null) {
            try {
                session = interceptor == null ? factory.openSession() : factory.withOptions().interceptor(interceptor).openSession();
            } catch (RuntimeException ex) {
                throw handleException(ex);
            }
        }

        return session;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Because this implementation decorates a delegate session, it removes the
     * delegate session, but it does allow re-referencing once close() has been
     * called. It simply grabs a new Hibernate session.
     * </p>
     */
    @Override
    public Connection close() {
        try {
            return getDelegate().close();
        } catch (HibernateException ex) {
            session = null;
            throw handleException(ex);
        } finally {
            session = null;
        }
    }

    @Override
    public void invalidateDelegate() {
        if (session != null) {
            try {
                session.clear();
                session.close();
            } catch (HibernateException ex) {
                session = null;
                throw handleException(ex);
            } finally {
                session = null;
            }
        }
    }

    /**
     * Returns the current interceptor.
     * 
     * @return The Interceptor
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /**
     * Sets a new hibernate session interceptor. This is only applicable if
     * there is no current session. If this session object has been used, then
     * please call close() first.
     * 
     * @param interceptor the Interceptor to apply to this session.
     * @throws IllegalStateException if this session has already been utilized
     *             after creation.
     */
    public void setInterceptor(final Interceptor interceptor) throws IllegalStateException {
        if (session != null) {
            throw new IllegalStateException("Cannot apply interceptor after session has been utilized");
        }

        this.interceptor = interceptor;
    }

    /**
     * Add some insurance against potential memory leaks. Make sure that Session
     * is closed.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (session != null) {
                session.close();
            }
        } finally {
            super.finalize();
        }
    }

    /** {@inheritDoc} * */
    @Override
    public int hashCode() {
        if (session == null) {
            return 13;
        }

        return session.hashCode();

    }

    /** {@inheritDoc} * */
    @Override
    public String toString() {
        return ScopedSession.class.getName() + " using current Session : " + session;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Currently does nothing. Session is lazily created .
     * </p>
     */
    public void start() {
        // currently does nothing. Session is lazily created
    }

    /**
     * {@inheritDoc}
     * <p>
     * Closes and invalidates any sessions that are still open.
     * </p>
     */
    public void stop() {
        this.close();

    }

    /**
     * {@inheritDoc}
     * <p>
     * Prevents any further utilization once called.
     * </p>
     */
    public void dispose() {
        if (this.session != null) {
            close();
        }

        disposed = true;
    }

	public boolean isDefaultReadOnly() {
		return session.isDefaultReadOnly();
	}

	public void setDefaultReadOnly(boolean readOnly) {
		session.setDefaultReadOnly(readOnly);
	}

	public Object load(Class theClass, Serializable id, LockOptions lockOptions)
			throws HibernateException {
		return session.load(theClass, id, lockOptions);
	}

	public Object load(String entityName, Serializable id,
			LockOptions lockOptions) throws HibernateException {
		return session.load(entityName, id,lockOptions);
	}

	public LockRequest buildLockRequest(LockOptions lockOptions) {
		return session.buildLockRequest(lockOptions);
	}

	public void refresh(Object object, LockOptions lockOptions)
			throws HibernateException {
		session.refresh(object,lockOptions);
	}

	public Object get(Class clazz, Serializable id, LockOptions lockOptions)
			throws HibernateException {
		return session.get(clazz, id, lockOptions);
	}

	public Object get(String entityName, Serializable id,
			LockOptions lockOptions) throws HibernateException {
		return session.get(entityName,id,lockOptions);
	}

	public boolean isReadOnly(Object entityOrProxy) {
		return session.isReadOnly(entityOrProxy);
	}

	public void doWork(Work work) throws HibernateException {
		session.doWork(work);
	}

	public boolean isFetchProfileEnabled(String name)
			throws UnknownProfileException {
		return session.isFetchProfileEnabled(name);
	}

	public void enableFetchProfile(String name) throws UnknownProfileException {
		session.enableFetchProfile(name);
	}

	public void disableFetchProfile(String name) throws UnknownProfileException {
		session.disableFetchProfile(name);
	}

	public TypeHelper getTypeHelper() {
		return session.getTypeHelper();
	}

	public LobHelper getLobHelper() {
		return session.getLobHelper();
	}

}
