/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.containers.TransientPicoContainer;

public class PicoWebAppContext extends WebAppContext {
    private final PicoContainer parentContainer;


    public PicoWebAppContext(final PicoContainer parentContainer) {
             super(new SessionHandler(),new ConstraintSecurityHandler(),new ServletHandler(),null);
        this.parentContainer = parentContainer;
        _scontext = new PicoConstructionContext();
    }

    boolean doSuperIsRunning = true;

    @Override
    protected void loadConfigurations() throws Exception {
        super.loadConfigurations();
        Configuration[]  configurations = getConfigurations();
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i] instanceof WebXmlConfiguration) {
                configurations[i] = new WebXmlConfiguration();
            }
        }
        doSuperIsRunning = false;
        setConfigurations(configurations);
        doSuperIsRunning = true;
    }

    @Override
    public boolean isRunning() {
        if (doSuperIsRunning) {
            return super.isRunning();
        } else {
            return false;
        }
    }



    public class PicoConstructionContext extends WebAppContext.Context {

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Filter> T createFilter(final Class<T> c) throws ServletException {
			T f = PicoWebAppContext.this.parentContainer.getComponent(c);
			if (f == null) {
				f = (T) createFromTransientPico(c);
			}


			for (int i=_decorators.size()-1; i>=0; i--)
            {

                Decorator decorator = _decorators.get(i);
                f=decorator.decorateFilterInstance(f);
            }

			return f;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Servlet> T createServlet(final Class<T> c) throws ServletException {
			T f = PicoWebAppContext.this.parentContainer.getComponent(c);
			if (f == null) {
				f = (T) createFromTransientPico(c);
			}


			for (int i=_decorators.size()-1; i>=0; i--)
            {
                Decorator decorator = _decorators.get(i);
                f=decorator.decorateServletInstance(f);
            }

			return f;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends EventListener> T createListener(final Class<T> clazz) throws ServletException {
			T f = PicoWebAppContext.this.parentContainer.getComponent(clazz);
			if (f == null) {
				f = (T) createFromTransientPico(clazz);
			}


			for (int i=_decorators.size()-1; i>=0; i--)
            {

                Decorator decorator = _decorators.get(i);
                f=decorator.decorateListenerInstance(f);
            }

			return f;
		}

		private Object createFromTransientPico(final Class<?> clazz) {
			MutablePicoContainer child = new TransientPicoContainer(PicoWebAppContext.this.parentContainer);
			child.addComponent("component", clazz);
			return child.getComponent("component");
		}

	}
}
