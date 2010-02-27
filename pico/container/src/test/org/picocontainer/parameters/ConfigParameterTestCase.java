package org.picocontainer.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.NullCA;

/**
 * test that config parameter does the right job
 * 
 * @author Konstantin Pribluda
 * 
 */
public class ConfigParameterTestCase {


	// defaultparameter name, just for convenience
	NameBinding paramNameBinding = new NameBinding() {
		public String getName() {
			return "gloo.blum";
		}

	};

	@Test public void testThatNoEntryIsWorkingProperly() throws Exception {
		PicoContainer container = new DefaultPicoContainer();
		ComponentParameter parameter = new ComponentParameter("gloo.blum");

		// shall be not resolvable
		assertFalse(parameter.resolve(container, null, null, String.class,
                                           paramNameBinding, false, null).isResolved());

		// shall resolve instance as null
		assertNull(parameter.resolve(container, null, null, String.class,
                                             paramNameBinding, false, null).resolveInstance());
	}

	@Test public void testThatNotStringEntryIsNotResolved() throws Exception {
		MutablePicoContainer container = new DefaultPicoContainer();
		container.addComponent("gloo.blum", new Integer(239));

		ComponentParameter parameter = new ComponentParameter("gloo.blum");

		// shall be not resolvable
		assertFalse(parameter.resolve(container, null, null, String.class,
                                           paramNameBinding, false, null).isResolved());

		// shall resolve instance as null
		assertNull(parameter.resolve(container, null, null, String.class,
                                             paramNameBinding, false, null).resolveInstance());

	}

	/**
	 * shall resolve as ddifferent classes
	 * 
	 * @throws Exception
	 */
	@Test public void testThatResolvedSuccessfully() throws Exception {
		MutablePicoContainer container = new DefaultPicoContainer();
		container.addComponent("gloo.blum", "239");

		ComponentParameter parameter = new ComponentParameter("gloo.blum");

		assertEquals(new Integer(239), parameter.resolve(container,
				new NullCA(Integer.class), null, Integer.class, paramNameBinding, false, null).resolveInstance());
		assertEquals("239", parameter.resolve(container, new NullCA(String.class),
                null, String.class, paramNameBinding, false, null).resolveInstance());
	}

	/**
	 * shall bomb properly if no suitable converter found
	 * 
	 */
	@Test public void testThatUnavailableConverterProducesCorrectException() {
		MutablePicoContainer container = new DefaultPicoContainer();
		container.addComponent("gloo.blum", "239");

		ComponentParameter parameter = new ComponentParameter("gloo.blum");

//		try {
//			Object foo = parameter.resolveInstance(container, null, List.class, paramNameBinding, false);
//			fail("failed to bomb on unavailable converter");
//		} catch (ConfigParameter.NoConverterAvailableException ex) {
//			// that's anticipated
//		}
	    Object foo = parameter.resolve(container, null, null, List.class, paramNameBinding, false, null).resolveInstance();
        assertNull(foo);

    }
	
	@Test public void testComponentInstantiation() {
		DefaultPicoContainer properties = new DefaultPicoContainer();
		properties.addComponent("numericProperty", "239");
		properties.addComponent("doubleProperty", "17.95");
		properties.addComponent("stringProperty", "foo.bar");

		DefaultPicoContainer container = new DefaultPicoContainer(properties);
		container.addComponent("configured", ExternallyConfiguredComponent.class,
						new ComponentParameter("numericProperty"),
						// resolves as string
						new ComponentParameter("stringProperty"),
						// resolves as file
						new ComponentParameter("stringProperty"),
						// resolves as double
						new ComponentParameter("doubleProperty")
					
				);
		
		
		ExternallyConfiguredComponent component = (ExternallyConfiguredComponent) container.getComponent("configured");
		
		assertNotNull(component);
		assertEquals(239,component.getLongValue());
		assertEquals("foo.bar",component.getStringParameter());
		assertEquals(new File("foo.bar"),component.getFileParameter());
		assertEquals(17.95,component.getDoubleParameter(),0);
	}

    @Test public void testComponentInstantiationViaParamNameAssociations() {
        DefaultPicoContainer properties = new DefaultPicoContainer();
        properties.addConfig("longValue", "239");
        properties.addConfig("doubleParameter", "17.95");
        properties.addConfig("stringParameter", "foo.bar");
        properties.addConfig("fileParameter", "bar.txt");

        DefaultPicoContainer container = new DefaultPicoContainer(properties);
        container.as(Characteristics.USE_NAMES).addComponent(ExternallyConfiguredComponent.class);

        ExternallyConfiguredComponent component = container.getComponent(ExternallyConfiguredComponent.class);
		
        assertNotNull(component);
        assertEquals(239,component.getLongValue());
        assertEquals("foo.bar",component.getStringParameter());
        assertEquals(new File("bar.txt"),component.getFileParameter());
        assertEquals(17.95,component.getDoubleParameter(),0);
    }



	/**
	 * test component to show automatic conversion
	 * 
	 * @author ko5tik
	 */

	public static class ExternallyConfiguredComponent {
		long longValue;

		String stringParameter;

		File fileParameter;

		double doubleParameter;

		public ExternallyConfiguredComponent(long longValue, String stringParameter, File fileParameter, double doubleParameter) {
			super();
			this.longValue = longValue;
			this.stringParameter = stringParameter;
			this.fileParameter = fileParameter;
			this.doubleParameter = doubleParameter;
		}

		public double getDoubleParameter() {
			return doubleParameter;
		}

		public File getFileParameter() {
			return fileParameter;
		}

		public long getLongValue() {
			return longValue;
		}

		public String getStringParameter() {
			return stringParameter;
		}

		public void setDoubleParameter(double doubleParameter) {
			this.doubleParameter = doubleParameter;
		}

		public void setFileParameter(File fileParameter) {
			this.fileParameter = fileParameter;
		}

		public void setLongValue(long longValue) {
			this.longValue = longValue;
		}

		public void setStringParameter(String stringParameter) {
			this.stringParameter = stringParameter;
		}

	}

}
