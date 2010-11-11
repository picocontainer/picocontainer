package org.picocontainer.modules.monitor.commonslogging;


import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class CommonsLoggingModuleMonitorTestCase {

	
	@Test
	public void testAllMethodsCanAcceptNullParametersWithoutCrashing() {
		CommonsLoggingModuleMonitor monitor = new CommonsLoggingModuleMonitor();
		monitor.compositionClassNotCorrectType(null, null, null);
		monitor.deploySuccess(null,null,0);
		monitor.errorPerformingDeploy(null, null);
		monitor.noCompositionClassFound(null, null, null, null);
	}
	
	@Test
	public void testEverythingWorksThroughSerialization() throws IOException, ClassNotFoundException {
		CommonsLoggingModuleMonitor monitor = new CommonsLoggingModuleMonitor();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(monitor);
		
		oos.close();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		
		CommonsLoggingModuleMonitor monitor2 = (CommonsLoggingModuleMonitor) ois.readObject();
		assertNotNull(monitor2);
		
		//Verify internal logger was properly reconstituted.
		monitor.deploySuccess(null, null, 42L);
	}

}
