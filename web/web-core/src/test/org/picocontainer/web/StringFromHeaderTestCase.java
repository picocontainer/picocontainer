package org.picocontainer.web;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

@RunWith(JMock.class)
public class StringFromHeaderTestCase {

	private Mockery context = new JUnit4Mockery();
	
	private HttpServletRequest request;
	
	@Test
	public void testHeaderNameSubstitution() {
		StringFromHeader header = new StringFromHeader("Content-Type");
		assertEquals("Content_Type", header.getComponentKey());
	}
	
	public static class Integration {
		public Integration(String User_Agent) {
			//Does nothing.
		}
	}

	@Test
	public void testPicoIntegration() {
		request = context.mock(HttpServletRequest.class);
		context.checking(new Expectations() {{
			atLeast(1).of(request).getHeader("User-Agent");
			will(returnValue("firefox"));
		}});
		
		MutablePicoContainer mpc = new PicoBuilder()
				.withCaching()
				.withLifecycle().build();
		
		
		mpc.addComponent(HttpServletRequest.class, request);
		mpc.addAdapter(new StringFromHeader("User-Agent"));
		mpc.addComponent(Integration.class);
		assertEquals("firefox", mpc.getComponent("User_Agent"));
		assertNotNull(mpc.getComponent(Integration.class));
	}

	@Test
	public void testToString() {
		StringFromHeader header = new StringFromHeader("User-Agent");
		String toString = header.toString();
		assertNotNull(toString);
		assertTrue(toString.contains("User-Agent"));
		assertTrue(toString.contains("User_Agent"));
	}

}
