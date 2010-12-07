package org.picocontainer.script;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.script.xml.XMLContainerBuilder;

public class ScriptedContainerBuilderFactoryTestCase {

	
	@Test
	public void testSubstitutionOfDefaultAutoStartingBuildAction() {
		ClassLoader cl = ScriptedContainerBuilderFactoryTestCase.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("org/picocontainer/script/picocontainer.xml");
		assertNotNull(is);
		
		ScriptedContainerBuilderFactory factory = new ScriptedContainerBuilderFactory(new InputStreamReader(is), XMLContainerBuilder.class.getName(), cl);
		factory.setDefaultPostBuildAction(new NoOpPostBuildContainerAction());
		
		MutablePicoContainer pico = (MutablePicoContainer) factory.getContainerBuilder().buildContainer(null, null, false);
		assertFalse("Got " + pico.getLifecycleState().toString(), pico.getLifecycleState().isStarted());
	}
}
