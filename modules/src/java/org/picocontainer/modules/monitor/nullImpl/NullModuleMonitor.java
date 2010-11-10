/**
 * 
 */
package org.picocontainer.modules.monitor.nullImpl;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.adapter.ModuleMonitor;

/**
 * @author Mike
 *
 */
public class NullModuleMonitor implements ModuleMonitor {

	public void noCompositionClassFound(FileObject applicationFolder,
			String className, ClassLoader moduleClassLoader,
			ClassNotFoundException e) {
		//No-op
	}

	public void compositionClassNotCorrectType(FileObject applicationFolder,
			Class<?> compositionClass, ClassLoader moduleClassLoader) {
		//No-op
	}

	public void errorPerformingDeploy(FileObject applicationFolder, Throwable e) {
		//No-op
	}

	public void deploySuccess(FileObject applicationFolder,
			MutablePicoContainer returnValue, long timeInMillis) {
		//No-op
	}

}
