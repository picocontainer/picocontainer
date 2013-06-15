package org.picocontainer.jetty;

import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.containers.TransientPicoContainer;

public class PicoServletContextHandler extends ServletContextHandler {

	private final PicoContainer pico;

	public PicoServletContextHandler(PicoContainer pico) {
		super();
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public PicoServletContextHandler(PicoContainer pico,int options) {
		super(options);
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public PicoServletContextHandler(PicoContainer pico, HandlerContainer parent, String contextPath) {
		super(parent, contextPath);
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public PicoServletContextHandler(PicoContainer pico, HandlerContainer parent, String contextPath, int options) {
		super(parent, contextPath, options);
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public PicoServletContextHandler(PicoContainer pico, HandlerContainer parent, String contextPath, boolean sessions, boolean security) {
		super(parent, contextPath, sessions, security);
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public PicoServletContextHandler(PicoContainer pico, HandlerContainer parent, SessionHandler sessionHandler,
			SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
		super(parent, sessionHandler, securityHandler, servletHandler, errorHandler);
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public PicoServletContextHandler(PicoContainer pico, HandlerContainer parent, String contextPath, SessionHandler sessionHandler,
			SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
		super(parent, contextPath, sessionHandler, securityHandler, servletHandler, errorHandler);
		this.pico = pico;
		_scontext = new PicoConstructionContext();
	}

	public class PicoConstructionContext extends ServletContextHandler.Context {

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
			T f = PicoServletContextHandler.this.pico.getComponent(c);
			if (f == null) {
				f = (T)createFromTransientPico(c);
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
		public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
			T f = PicoServletContextHandler.this.pico.getComponent(c);
			if (f == null) {
				f = (T)createFromTransientPico(c);
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
		public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
			T f = PicoServletContextHandler.this.pico.getComponent(clazz);
			if (f == null) {
				f = (T)createFromTransientPico(clazz);
			}
			
			
			for (int i=_decorators.size()-1; i>=0; i--)
            {
				
                Decorator decorator = _decorators.get(i);
                f=decorator.decorateListenerInstance(f);
            }
			
			return f;
		}
		
		
		private Object createFromTransientPico(Class<?> clazz) {
			MutablePicoContainer child = new TransientPicoContainer(PicoServletContextHandler.this.pico);
			child.addComponent("component", clazz);
			return child.getComponent("component");
		}		
    
	}
	
}
