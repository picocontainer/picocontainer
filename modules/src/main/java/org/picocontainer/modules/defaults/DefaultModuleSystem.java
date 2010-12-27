/**
 * 
 */
package org.picocontainer.modules.defaults;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.modules.ModuleMonitor;
import org.picocontainer.modules.PicoModuleSystem;
import org.picocontainer.modules.deployer.Deployer;
import org.picocontainer.script.util.MultiException;

/**
 * Default implementation of the multi module system.
 * 
 * @author Michael Rimov, Centerline Computers, Inc.
 * 
 */
public class DefaultModuleSystem implements PicoModuleSystem {

	private final ModuleMonitor monitor;
	private final Deployer deployer;
	private final FileObject moduleDirectory;
	private final ClassLoader classLoader;
	private MutablePicoContainer parent;
	private final List<MutablePicoContainer> builtContainers = new ArrayList<MutablePicoContainer>();

	public DefaultModuleSystem(final ModuleMonitor monitor,
			final Deployer deployer, final FileObject moduleDirectory,
			final ClassLoader classLoader) {
		this.monitor = monitor;
		this.deployer = deployer;
		this.moduleDirectory = moduleDirectory;
		this.classLoader = classLoader;
	}

	public synchronized DefaultModuleSystem deploy(
			final MutablePicoContainer parent) throws MultiException {
		if (this.parent != null) {
			throw new IllegalStateException(
					"This module system already appears"
							+ " to be deployed.  Current Parent pico is "
							+ this.parent);
		}

		this.parent = parent;
		final long startTime = System.currentTimeMillis();
		monitor.startingMultiModuleDeployment(moduleDirectory);
		final MultiException errors = new MultiException(
				"Pico Module Deployment");
		final MutablePicoContainer returnValue = parent;
		try {
			for (final FileObject eachChild : moduleDirectory.getChildren()) {
				try {
					if (!isDeployable(eachChild)) {
						monitor.skippingDeploymentBecauseNotDeployable(eachChild);
						continue;
					}

					final MutablePicoContainer result = deployer.deploy(
							eachChild, this.classLoader, parent, null);
					result.setName(eachChild.getName().getBaseName());
					this.builtContainers.add(result);
				} catch (final Exception ex) {
					errors.addException(ex);
				}
			}

		} catch (final FileSystemException ex) {
			errors.addException(ex);
		}

		try {
			parent.start();
		} catch (final Exception e) {
			errors.addException(e);
		}
		final long endTime = System.currentTimeMillis();
		if (errors.getErrorCount() > 0) {
			monitor.multiModuleDeploymentFailed(moduleDirectory, errors);
			throw errors;
		}
		monitor.multiModuleDeploymentSuccess(moduleDirectory, returnValue,
				endTime - startTime);
		return this;
	}

	public DefaultModuleSystem deploy() throws MultiException {
		final MutablePicoContainer basicPico = new PicoBuilder().withCaching()
				.withLifecycle().build();
		return deploy(basicPico);
	}

	public synchronized void undeploy() throws MultiException {
		if (parent == null) {
			throw new IllegalStateException(
					"Module system already appears to be undeployed");
		}

		final long startTime = System.currentTimeMillis();
		monitor.multiModuleUndeploymentBeginning(parent);
		final MultiException errors = new MultiException("undeploy " + parent);
		try {
			for (final MutablePicoContainer eachPico : this.builtContainers) {
				parent.removeChildContainer(eachPico);
				stopAndDisposePico(errors, eachPico);
			}
			// finally, kill the parent
			stopAndDisposePico(errors, parent);

		} finally {
			builtContainers.clear();
			parent = null;
		}

		if (errors.getErrorCount() > 0) {
			monitor.multiModuleUndeploymentFailure(errors,
					System.currentTimeMillis() - startTime);
			throw errors;
		} else {
			monitor.multiModuleUndeploymentSuccess(System.currentTimeMillis()
					- startTime);
		}

	}

	private void stopAndDisposePico(final MultiException errors,
			final MutablePicoContainer eachPico) {
		if (eachPico.getLifecycleState().isStarted()) {
			try {
				eachPico.stop();
			} catch (final RuntimeException e) {
				errors.addException(e);
			}
		}

		if (!eachPico.getLifecycleState().isDisposed()) {
			try {
				eachPico.dispose();
			} catch (final RuntimeException e) {
				errors.addException(e);
			}
		}
	}

	protected boolean isDeployable(final FileObject allChildren) {
		return true;
	}

	public synchronized MutablePicoContainer getPico()
			throws IllegalStateException {
		if (parent == null) {
			throw new IllegalStateException("Module System is not deployed");
		}

		return parent;
	}

}
