/**
 * 
 */
package org.picocontainer.modules.monitor.nullImpl;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;

/**
 * @author Mike
 * 
 */
@SuppressWarnings("serial")
public class NullModuleMonitor implements ModuleMonitor {

	public void noCompositionClassFound(final FileObject applicationFolder,
			final String className, final ClassLoader moduleClassLoader,
			final ClassNotFoundException e) {
		// No-op
	}

	public void compositionClassNotCorrectType(
			final FileObject applicationFolder,
			final Class<?> compositionClass, final ClassLoader moduleClassLoader) {
		// No-op
	}

	public void errorPerformingDeploy(final FileObject applicationFolder,
			final Throwable e) {
		// No-op
	}

	public void deploySuccess(final FileObject applicationFolder,
			final MutablePicoContainer returnValue, final long timeInMillis) {
		// No-op
	}

}
