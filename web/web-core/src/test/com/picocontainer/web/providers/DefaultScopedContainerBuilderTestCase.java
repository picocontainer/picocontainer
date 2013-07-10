package com.picocontainer.web.providers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.behaviors.Storing;
import com.picocontainer.web.ScopedContainers;

public class DefaultScopedContainerBuilderTestCase {


	@Test
	public void testMakeScopedContainers() {
		DefaultScopedContainerBuilder containerBuilder = new DefaultScopedContainerBuilder();
		ScopedContainers containers = containerBuilder.makeScopedContainers(false);
		assertNotNull(getRequestStoring(containers));
		assertNotNull(getSessionStoring(containers));
		assertNotNull(getAppContainer(containers));
		assertNotNull(getSessionContainer(containers));
		assertNotNull(getRequestContainer(containers));
	}
	
	@Test
	public void testMakeStatelessScopedContainers() {
		DefaultScopedContainerBuilder containerBuilder = new DefaultScopedContainerBuilder();
		ScopedContainers containers = containerBuilder.makeScopedContainers(true);
		assertNotNull(getRequestStoring(containers));
		assertNull(getSessionStoring(containers));
		assertNotNull(getAppContainer(containers));
		assertNull(getSessionContainer(containers));
		assertNotNull(getRequestContainer(containers));		
	}

	
	static Storing getRequestStoring(ScopedContainers containers) {
		Storing result = (Storing)invokeGetterMethod(containers, "getRequestStoring");
		return result;
	}

	static Storing getSessionStoring(ScopedContainers containers) {
		Storing result = (Storing)invokeGetterMethod(containers, "getSessionStoring");
		return result;		
	}
	
	static MutablePicoContainer getAppContainer(ScopedContainers containers) {
		return (MutablePicoContainer)invokeGetterMethod(containers, "getApplicationContainer");
	}
	
	static MutablePicoContainer getSessionContainer(ScopedContainers containers) {
		return (MutablePicoContainer)invokeGetterMethod(containers, "getSessionContainer");
	}
	
	static MutablePicoContainer getRequestContainer(ScopedContainers containers) {
		return (MutablePicoContainer)invokeGetterMethod(containers, "getRequestContainer");
	}
	
	
	static Object invokeGetterMethod(ScopedContainers containers, String methodName) {
		Object result;
		try {
			Method m = ScopedContainers.class.getDeclaredMethod(methodName);
			m.setAccessible(true);
			result =  m.invoke(containers);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
}
