/*******************************************************************************
 * Copyright (C)  PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.modules.monitor.nullImpl;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.script.util.MultiException;

/**
 * Null Monitor that just accepts the callbacks and does nothing.
 * @author Michael Rimov, Centerline Computers Inc.
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
	public void skippingDeploymentBecauseNotDeployable(
			final FileObject allChildren) {
		// No-op

	}

	/** {@inheritDoc} **/
	public void startingMultiModuleDeployment(final FileObject moduleDirectory) {
		// No-op

	}

	/** {@inheritDoc} **/
	public void multiModuleDeploymentFailed(final FileObject moduleDirectory,
			final MultiException errors) {
		// No-op

	}

	/** {@inheritDoc} **/
	public void multiModuleDeploymentSuccess(final FileObject moduleDirectory,
			final MutablePicoContainer returnValue, final long deploymentTime) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void multiModuleUndeploymentBeginning(
			final MutablePicoContainer parent) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void multiModuleUndeploymentSuccess(final long l) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void multiModuleUndeploymentFailure(final MultiException errors,
			final long l) {
		// No-op
	}

	/** {@inheritDoc} **/
	public void moduleAlreadyDeployed(final String moduleName) {
		// No-op
	}

}
