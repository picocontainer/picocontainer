package com.picocontainer.web.el;

import javax.el.ELContext;

import com.picocontainer.PicoContainer;

public class JspPicoElResolver extends AbstractPicoElResolver {
	
	private PicoHook picoHook;

	public JspPicoElResolver() {
		picoHook = new PicoHook();
	}

	@Override
	protected final PicoContainer getPicoContainer(final ELContext context) {
		return picoHook.getCurrentRequestPico();
	}

	
	private static class PicoHook extends com.picocontainer.web.PicoServletFilter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PicoContainer getCurrentRequestPico() {
			return super.getRequestContainer();
		}
	}
}
