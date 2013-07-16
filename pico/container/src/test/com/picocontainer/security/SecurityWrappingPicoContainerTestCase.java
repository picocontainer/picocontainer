package com.picocontainer.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.Collections;
import java.util.Properties;

import javax.inject.Provider;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.Parameter;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.lifecycle.LifecycleState;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;
import com.picocontainer.visitors.AbstractPicoVisitor;
import com.picocontainer.visitors.TraversalCheckingVisitor;

@RunWith(JMock.class)
public class SecurityWrappingPicoContainerTestCase {
	
	private Mockery context = new JUnit4Mockery();

	private MutablePicoContainer delegate;
	
	private static final Permission readPermission = new PicoAccessPermission(null, "read");
	
	private static final Permission writePermission = new PicoAccessPermission(null, "write");

	
	private SecurityWrappingPicoContainer pico;

	public static class ReadAllowedAccessControllerWrapper implements AccessControllerWrapper {
		public void checkPermission(Permission checkingPermission) throws AccessControlException {
			if (readPermission.implies(checkingPermission)) {
				return;
			}
			
			throw new AccessControlException("Write not allowed");
		}
	}
	
	public static class WriteAllowedAccessControllerWrapper implements AccessControllerWrapper {
		public void checkPermission(Permission checkingPermission) throws AccessControlException {
			if (writePermission.implies(checkingPermission)) {
				return;
			}
			
			throw new AccessControlException("Write not allowed");
		}
	}
	
	public static class ReadWriteAllowedAccessControllerWrapper implements AccessControllerWrapper {
		public void checkPermission(Permission checkingPermission) throws AccessControlException {
			if (readPermission.implies(checkingPermission)  || writePermission.implies(checkingPermission)) {
				return;
			}
			
			throw new AccessControlException("Write not allowed");
		}
		
	}
	
	public static class DummyVisitor implements PicoVisitor {

		public boolean visitContainer(PicoContainer pico) {
			return false;
		}

		public void visitComponentAdapter(ComponentAdapter<?> componentAdapter) {
		}

		public void visitComponentFactory(ComponentFactory componentFactory) {
		}

		public void visitParameter(Parameter parameter) {
		}

		public Object traverse(Object node) {
			return null;
		}
		
	}
	
		
	
	@Before
	public void setUp() throws Exception {
		delegate = context.mock(MutablePicoContainer.class);
		pico = new SecurityWrappingPicoContainer(null, delegate);
		
	}
	
	private void setReadsAllowed() throws NoSuchFieldException, IllegalAccessException {
		Field targetField = SecurityWrappingPicoContainer.class.getDeclaredField("accessWrapper");
		targetField.setAccessible(true);
		targetField.set(pico, new ReadAllowedAccessControllerWrapper());
	}
	
	private void setWritesAllowed() throws NoSuchFieldException, IllegalAccessException {
		Field targetField = SecurityWrappingPicoContainer.class.getDeclaredField("accessWrapper");
		targetField.setAccessible(true);
		targetField.set(pico, new WriteAllowedAccessControllerWrapper());
	}
	private void setReadWriteAllowed() throws NoSuchFieldException, IllegalAccessException {
		Field targetField = SecurityWrappingPicoContainer.class.getDeclaredField("accessWrapper");
		targetField.setAccessible(true);
		targetField.set(pico, new ReadWriteAllowedAccessControllerWrapper());
	}
	

	@SuppressWarnings("unchecked")
	public static void addReadDelegateMethods(final MutablePicoContainer testDelegate, Mockery testContext) {
		
		testContext.checking(new Expectations() {{
			oneOf(testDelegate).getComponent(with(any(Object.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).getComponentInto(with(any(Object.class)), with(any(Type.class)));
			will(returnValue(null));

			oneOf(testDelegate).getComponentInto(with(any(Class.class)), with(any(Type.class)));
			will(returnValue(null));

			oneOf(testDelegate).getComponentInto(with(any(Generic.class)), with(any(Type.class)));
			will(returnValue(null));

			oneOf(testDelegate).getComponent(with(any(Class.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).getComponent(with(any(Generic.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).getComponent(with(any(Class.class)), with(any(Class.class)), with(any(Type.class)));
			will(returnValue(null));
		
			oneOf(testDelegate).getComponent(with(any(Class.class)), with(any(Class.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).getComponents();
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getParent();
			will(returnValue(null));
			
			oneOf(testDelegate).getComponentAdapter(with(any(Object.class)));
			will(returnValue(null));
		
			oneOf(testDelegate).getComponentAdapter(with(any(Class.class)), with(any(NameBinding.class)));
			will(returnValue(null));

			oneOf(testDelegate).getComponentAdapter(with(any(Generic.class)), with(any(NameBinding.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).getComponentAdapter(with(any(Class.class)), with(any(Class.class)));
			will(returnValue(null));
		
			
			oneOf(testDelegate).getComponentAdapter(with(any(Generic.class)), with(any(Class.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).getComponentAdapters();
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getComponentAdapters(with(any(Class.class)));
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getComponentAdapters(with(any(Generic.class)));
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getComponentAdapters(with(any(Class.class)), with(any(Class.class)));
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getComponentAdapters(with(any(Generic.class)), with(any(Class.class)));
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getComponents(with(any(Class.class)));
			will(returnValue(Collections.emptyList()));
			
			oneOf(testDelegate).getName();
			will(returnValue(""));
			
			
			oneOf(testDelegate).getLifecycleState();
			will(returnValue(null));
			
			
		}});
		
	}
	
	@SuppressWarnings("unchecked")
	public static void addWriteDelegateMethods(final MutablePicoContainer testDelegate, Mockery testContext) {
		testContext.checking(new Expectations() {{
			oneOf(testDelegate).dispose();
			oneOf(testDelegate).start();
			oneOf(testDelegate).stop();
			
			oneOf(testDelegate).bind(with(any(Class.class)));
			will(returnValue(null));

			oneOf(testDelegate).addComponent(with(any(Object.class)), with(any(Object.class)), with(any(Parameter[].class)));
			will(returnValue(null));
			
			oneOf(testDelegate).addComponent(with(any(Object.class)), with(any(Object.class)), 
					with(any(ConstructorParameters.class)), with(any(FieldParameters[].class)), with(any(MethodParameters[].class)) );
			will(returnValue(null));
		
			oneOf(testDelegate).addComponent(with(any(Object.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).addConfig(with(any(String.class)), with(any(Object.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).addAdapter(with(any(ComponentAdapter.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).addProvider(with(any(Provider.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).addProvider(with(any(Object.class)), with(any(Provider.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).removeComponent(with(any(Object.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).removeComponentByInstance(with(any(Object.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).makeChildContainer();
			will(returnValue(null));
			
			oneOf(testDelegate).addChildContainer(with(any(PicoContainer.class)));
			will(returnValue(null));
			
			oneOf(testDelegate).removeChildContainer(with(any(PicoContainer.class)));
			will(returnValue(true));
			
			oneOf(testDelegate).change(with(any(Properties[].class)));
			with(returnValue(null));

			oneOf(testDelegate).as(with(any(Properties[].class)));
			with(returnValue(null));
			
			oneOf(testDelegate).setName(with(any(String.class)));
			
			oneOf(testDelegate).setLifecycleState(with(any(LifecycleState.class)));
			
			oneOf(testDelegate).changeMonitor(with(any(ComponentMonitor.class)));
			will(returnValue(null));
		}});
	}
	
	public static void addReadWriteDelegateMethods(final MutablePicoContainer testDelegate, Mockery testContext) {
		testContext.checking(new Expectations() {{
			oneOf(testDelegate).accept(with(any(PicoVisitor.class)));
		}});
	}
	
	@After
	public void tearDown() throws Exception {
		pico = null;
		delegate = null;
	}
	
	@Test
	public void testDefaultOfNoSecurityManagerInstalled() throws Exception {
		
		addReadDelegateMethods(delegate, context);
		addWriteDelegateMethods(delegate, context);
		addReadWriteDelegateMethods(delegate, context);
		
		expectSuccess("dispose", new Object[0]);
		expectSuccess("getComponent", new Object[] {null}, Object.class);
		expectSuccess("start", new Object[0]);
		expectSuccess("stop", new Object[0]);
		expectSuccess("bind", new Object[] {null}, Class.class);
		expectSuccess("getComponent", new Object[] {null}, Class.class);
		expectSuccess("addComponent", new Object[] {null, null, new Parameter[] {}}, Object.class, Object.class, Parameter[].class);
		expectSuccess("getComponent", new Object[] {null}, Generic.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Object.class, Type.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Generic.class, Type.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Class.class, Type.class);
		expectSuccess("getComponent", new Object[] {null, null, null}, Class.class, Class.class, Type.class);
		expectSuccess("getComponent", new Object[] {null, null}, Class.class, Class.class);
		expectSuccess("getComponents", new Object[0]);
		expectSuccess("getParent", new Object[0]);
		
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Class.class, Class.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Class.class, NameBinding.class);
		
		expectSuccess("getComponentAdapter", new Object[]{null}, Object.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Generic.class, Class.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Generic.class, NameBinding.class);
		
		expectSuccess("addComponent", new Object[] {null, null,null, null, null}, Object.class, Object.class, ConstructorParameters.class, FieldParameters[].class, MethodParameters[].class);
		expectSuccess("addComponent", new Object[] {null}, Object.class);
		expectSuccess("addConfig", new Object[] {null, null}, String.class, Object.class);
		expectSuccess("addAdapter", new Object[] {null}, ComponentAdapter.class);
		expectSuccess("getComponentAdapters", new Object[0]);
		expectSuccess("addProvider", new Object[]{null}, Provider.class);
		expectSuccess("getComponentAdapters", new Object[]{null}, Class.class);
		expectSuccess("addProvider", new Object[]{null, null}, Object.class, Provider.class);
		expectSuccess("removeComponent", new Object[]{null}, Object.class);
		expectSuccess("getComponentAdapters", new Object[]{null}, Generic.class);
		expectSuccess("getComponentAdapters", new Object[]{null, null}, Class.class, Class.class);
		expectSuccess("removeComponentByInstance", new Object[]{null}, Object.class);
		expectSuccess("makeChildContainer", new Object[0]);
		expectSuccess("getComponentAdapters", new Object[]{null, null}, Generic.class, Class.class);
		expectSuccess("getComponents", new Object[]{null}, Class.class);
		expectSuccess("accept", new Object[]{new DummyVisitor()}, PicoVisitor.class);
		expectSuccess("addChildContainer", new Object[]{null}, PicoContainer.class);
		expectSuccess("removeChildContainer", new Object[]{null}, PicoContainer.class);
		expectSuccess("change", new Object[]{null}, Properties[].class);
		expectSuccess("as", new Object[]{null}, Properties[].class);
		expectSuccess("setName", new Object[]{null}, String.class);
		expectSuccess("setLifecycleState", new Object[]{null}, LifecycleState.class);
		expectSuccess("getName", new Object[0]);
		expectSuccess("getLifecycleState", new Object[0]);
		expectSuccess("changeMonitor", new Object[]{null}, ComponentMonitor.class);
		
	}

	@Test
	public void testOnlyReadMethodsAllowed() throws Exception {
		setReadsAllowed();
		addReadDelegateMethods(delegate, context);

		expectFailure("dispose", new Object[0]);
		expectSuccess("getComponent", new Object[] {null}, Object.class);
		expectFailure("start", new Object[0]);
		expectFailure("stop", new Object[0]);
		expectSuccess("getComponentInto", new Object[] {null, null}, Object.class, Type.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Class.class, Type.class);
		expectSuccess("getComponent", new Object[] {null}, Class.class);
		expectFailure("addComponent", new Object[] {null, null, new Parameter[] {}}, Object.class, Object.class, Parameter[].class);
		expectSuccess("getComponent", new Object[] {null}, Generic.class);
		expectFailure("bind", new Object[] {null}, Class.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Generic.class, Type.class);
		expectSuccess("getComponent", new Object[] {null, null, null}, Class.class, Class.class, Type.class);
		expectSuccess("getComponent", new Object[] {null, null}, Class.class, Class.class);
		expectSuccess("getComponents", new Object[0]);
		expectSuccess("getParent", new Object[0]);
		
		expectSuccess("getComponentAdapter", new Object[]{null}, Object.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Class.class, NameBinding.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Generic.class, NameBinding.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Class.class, Class.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Generic.class, Class.class);

		expectFailure("addComponent", new Object[] {null, null,null, null, null}, Object.class, Object.class, ConstructorParameters.class, FieldParameters[].class, MethodParameters[].class);
		expectFailure("addComponent", new Object[] {null}, Object.class);
		expectFailure("addConfig", new Object[] {null, null}, String.class, Object.class);
		expectFailure("addAdapter", new Object[] {null}, ComponentAdapter.class);
		expectSuccess("getComponentAdapters", new Object[0]);
		expectFailure("addProvider", new Object[]{null}, Provider.class);
		expectSuccess("getComponentAdapters", new Object[]{null}, Class.class);
		expectFailure("addProvider", new Object[]{null, null}, Object.class, Provider.class);
		expectFailure("removeComponent", new Object[]{null}, Object.class);
		expectSuccess("getComponentAdapters", new Object[]{null}, Generic.class);
		expectSuccess("getComponentAdapters", new Object[]{null, null}, Class.class, Class.class);
		expectFailure("removeComponentByInstance", new Object[]{null}, Object.class);
		expectFailure("makeChildContainer", new Object[0]);
		expectSuccess("getComponentAdapters", new Object[]{null, null}, Generic.class, Class.class);
		expectSuccess("getComponents", new Object[]{null}, Class.class);
		expectFailure("accept", new Object[]{new DummyVisitor()}, PicoVisitor.class);
		expectFailure("addChildContainer", new Object[]{null}, PicoContainer.class);
		expectFailure("removeChildContainer", new Object[]{null}, PicoContainer.class);
		expectFailure("change", new Object[]{null}, Properties[].class);
		expectFailure("as", new Object[]{null}, Properties[].class);
		expectFailure("setName", new Object[]{null}, String.class);
		expectFailure("setLifecycleState", new Object[]{null}, LifecycleState.class);
		expectSuccess("getName", new Object[0]);
		expectSuccess("getLifecycleState", new Object[0]);
		expectFailure("changeMonitor", new Object[]{null}, ComponentMonitor.class);
	
	}

	
	@Test
	public void testOnlyWriteMethodsAllowed() throws Exception {
		setWritesAllowed();

		addWriteDelegateMethods(delegate, context);
		expectSuccess("dispose", new Object[0]);
		expectFailure("getComponent", new Object[] {null}, Object.class);
		expectSuccess("start", new Object[0]);
		expectSuccess("stop", new Object[0]);
		expectFailure("getComponentInto", new Object[] {null, null}, Object.class, Type.class);
		expectFailure("getComponentInto", new Object[] {null, null}, Class.class, Type.class);
		expectFailure("getComponent", new Object[] {null}, Class.class);
		expectSuccess("addComponent", new Object[] {null, null, new Parameter[] {}}, Object.class, Object.class, Parameter[].class);
		expectFailure("getComponent", new Object[] {null}, Generic.class);
		expectSuccess("bind", new Object[] {null}, Class.class);
		expectFailure("getComponentInto", new Object[] {null, null}, Generic.class, Type.class);
		expectFailure("getComponent", new Object[] {null, null, null}, Class.class, Class.class, Type.class);
		expectFailure("getComponent", new Object[] {null, null}, Class.class, Class.class);
		expectFailure("getComponents", new Object[0]);
		expectFailure("getParent", new Object[0]);
		expectFailure("getComponentAdapter", new Object[]{null}, Object.class);
		expectFailure("getComponentAdapter", new Object[] {null, null}, Class.class, NameBinding.class);
		expectSuccess("addComponent", new Object[] {null, null,null, null, null}, Object.class, Object.class, ConstructorParameters.class, FieldParameters[].class, MethodParameters[].class);
		expectFailure("getComponentAdapter", new Object[] {null, null}, Generic.class, NameBinding.class);
		expectSuccess("addComponent", new Object[] {null}, Object.class);
		expectSuccess("addConfig", new Object[] {null, null}, String.class, Object.class);
		expectSuccess("addAdapter", new Object[] {null}, ComponentAdapter.class);
		expectFailure("getComponentAdapter", new Object[] {null, null}, Generic.class, Class.class);
		expectFailure("getComponentAdapters", new Object[0]);
		expectSuccess("addProvider", new Object[]{null}, Provider.class);
		expectFailure("getComponentAdapters", new Object[]{null}, Class.class);
		expectSuccess("addProvider", new Object[]{null, null}, Object.class, Provider.class);
		expectSuccess("removeComponent", new Object[]{null}, Object.class);
		expectFailure("getComponentAdapters", new Object[]{null}, Generic.class);
		expectFailure("getComponentAdapters", new Object[]{null, null}, Class.class, Class.class);
		expectSuccess("removeComponentByInstance", new Object[]{null}, Object.class);
		expectSuccess("makeChildContainer", new Object[0]);
		expectFailure("getComponentAdapters", new Object[]{null, null}, Generic.class, Class.class);
		expectFailure("getComponents", new Object[]{null}, Class.class);
		expectFailure("accept", new Object[]{new DummyVisitor()}, PicoVisitor.class);
		expectSuccess("addChildContainer", new Object[]{null}, PicoContainer.class);
		expectSuccess("removeChildContainer", new Object[]{null}, PicoContainer.class);
		expectSuccess("change", new Object[]{null}, Properties[].class);
		expectSuccess("as", new Object[]{null}, Properties[].class);
		expectSuccess("setName", new Object[]{null}, String.class);
		expectSuccess("setLifecycleState", new Object[]{null}, LifecycleState.class);
		expectFailure("getName", new Object[0]);
		expectFailure("getLifecycleState", new Object[0]);
		expectSuccess("changeMonitor", new Object[]{null}, ComponentMonitor.class);
		expectFailure("getComponentAdapter", new Object[] {null, null}, Class.class, Class.class);
		expectFailure("getComponentAdapter", new Object[] {null, null}, Class.class, NameBinding.class);
	}
	
	/**
	 * Only difference with this one and the other security values is that the visitor should 
	 * pass test too
	 */
	@Test
	public void testReadWriteAllowed() throws Exception {
		setReadWriteAllowed();
		addReadDelegateMethods(delegate, context);
		addWriteDelegateMethods(delegate, context);
		addReadWriteDelegateMethods(delegate, context);
		
		expectSuccess("dispose", new Object[0]);
		expectSuccess("getComponent", new Object[] {null}, Object.class);
		expectSuccess("start", new Object[0]);
		expectSuccess("stop", new Object[0]);
		expectSuccess("bind", new Object[] {null}, Class.class);
		expectSuccess("getComponent", new Object[] {null}, Class.class);
		expectSuccess("addComponent", new Object[] {null, null, new Parameter[] {}}, Object.class, Object.class, Parameter[].class);
		expectSuccess("getComponent", new Object[] {null}, Generic.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Object.class, Type.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Generic.class, Type.class);
		expectSuccess("getComponentInto", new Object[] {null, null}, Class.class, Type.class);
		expectSuccess("getComponent", new Object[] {null, null, null}, Class.class, Class.class, Type.class);
		expectSuccess("getComponent", new Object[] {null, null}, Class.class, Class.class);
		expectSuccess("getComponents", new Object[0]);
		expectSuccess("getParent", new Object[0]);
		
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Class.class, Class.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Class.class, NameBinding.class);
		
		expectSuccess("getComponentAdapter", new Object[]{null}, Object.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Generic.class, Class.class);
		expectSuccess("getComponentAdapter", new Object[] {null, null}, Generic.class, NameBinding.class);
		
		expectSuccess("addComponent", new Object[] {null, null,null, null, null}, Object.class, Object.class, ConstructorParameters.class, FieldParameters[].class, MethodParameters[].class);
		expectSuccess("addComponent", new Object[] {null}, Object.class);
		expectSuccess("addConfig", new Object[] {null, null}, String.class, Object.class);
		expectSuccess("addAdapter", new Object[] {null}, ComponentAdapter.class);
		expectSuccess("getComponentAdapters", new Object[0]);
		expectSuccess("addProvider", new Object[]{null}, Provider.class);
		expectSuccess("getComponentAdapters", new Object[]{null}, Class.class);
		expectSuccess("addProvider", new Object[]{null, null}, Object.class, Provider.class);
		expectSuccess("removeComponent", new Object[]{null}, Object.class);
		expectSuccess("getComponentAdapters", new Object[]{null}, Generic.class);
		expectSuccess("getComponentAdapters", new Object[]{null, null}, Class.class, Class.class);
		expectSuccess("removeComponentByInstance", new Object[]{null}, Object.class);
		expectSuccess("makeChildContainer", new Object[0]);
		expectSuccess("getComponentAdapters", new Object[]{null, null}, Generic.class, Class.class);
		expectSuccess("getComponents", new Object[]{null}, Class.class);
		expectSuccess("accept", new Object[]{new DummyVisitor()}, PicoVisitor.class);
		expectSuccess("addChildContainer", new Object[]{null}, PicoContainer.class);
		expectSuccess("removeChildContainer", new Object[]{null}, PicoContainer.class);
		expectSuccess("change", new Object[]{null}, Properties[].class);
		expectSuccess("as", new Object[]{null}, Properties[].class);
		expectSuccess("setName", new Object[]{null}, String.class);
		expectSuccess("setLifecycleState", new Object[]{null}, LifecycleState.class);
		expectSuccess("getName", new Object[0]);
		expectSuccess("getLifecycleState", new Object[0]);
		expectSuccess("changeMonitor", new Object[]{null}, ComponentMonitor.class);		
	}
	
	private void expectSuccess(String methodName, Object[] args, Class<?>... argTypes) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		invokeMethod(methodName, args, argTypes);
	}

	
	private void expectFailure(String methodName, Object[] args, Class<?>... argTypes) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		try {
			invokeMethod(methodName, args, argTypes);
			fail("Expected Failure");
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof AccessControlException) {
				assertNotNull(e.getCause().getMessage());
			} else {
				throw e;
			}
		}
	}
	
	private void invokeMethod(String methodName, Object[] args, Class<?>... argTypes) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method m = pico.getClass().getMethod(methodName, argTypes);
		m.invoke(pico, args);
	}
}
