/**
 * 
 */
package org.picocontainer.modules.monitor.nullImpl;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.script.util.MultiException;

/**
 * @author Mike
 * 
 */
@SuppressWarnings("serial")
public class NullModuleMonitor implements ModuleMonitor {

	/** {@inheritDoc} **/
	public void noCompositionClassFound(final FileObject applicationFolder,
			final String className, final ClassLoader moduleClassLoader,
			final ClassNotFoundException e) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void compositionClassNotCorrectType(
			final FileObject applicationFolder,
			final Class<?> compositionClass, final ClassLoader moduleClassLoader) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void errorPerformingDeploy(final FileObject applicationFolder,
			final Throwable e) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void deploySuccess(final FileObject applicationFolder,
			final MutablePicoContainer returnValue, final long timeInMillis) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void skippingDeploymentBecauseNotDeployable(FileObject allChildren) {
		// No-op
		
	}

	/** {@inheritDoc} **/
	public void startingMultiModuleDeployment(FileObject moduleDirectory) {
		// No-op
		
	}

	/** {@inheritDoc} **/
	public void multiModuleDeploymentFailed(FileObject moduleDirectory,
			MultiException errors) {
		// No-op
		
	}

	/** {@inheritDoc} **/
	public void multiModuleDeploymentSuccess(FileObject moduleDirectory,
			MutablePicoContainer returnValue, long deploymentTime) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void multiModuleUndeploymentBeginning(MutablePicoContainer parent) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void multiModuleUndeploymentSuccess(long l) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void multiModuleUndeploymentFailure(MultiException errors, long l) {
		// No-op
	}

}
