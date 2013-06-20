package org.picocontainer.web.el;

import javax.el.ELContext;

import org.picocontainer.PicoContainer;

public class JspPicoElResolver extends AbstractPicoElResolver {
	
	private PicoHook picoHook;

	public JspPicoElResolver() {
		picoHook = new PicoHook();
	}

	@Override
	protected final PicoContainer getPicoContainer(final ELContext context) {
		return picoHook.getCurrentRequestPico();
	}

	
	private static class PicoHook extends org.picocontainer.web.PicoServletFilter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PicoContainer getCurrentRequestPico() {
			return super.getRequestContainer();
		}
	}
}
