/**
 * 
 */
package com.picocontainer.modules.defaults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import com.picocontainer.modules.CircularDependencyException;
import com.picocontainer.modules.DeploymentException;
import com.picocontainer.modules.FileSystemRuntimeException;
import com.picocontainer.modules.ModuleMonitor;
import com.picocontainer.modules.PicoModuleSystem;
import com.picocontainer.modules.deployer.Deployer;
import com.picocontainer.script.util.MultiException;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;

/**
 * Default implementation of the multi module system.
 * 
 * @author Michael Rimov, Centerline Computers, Inc.
 * 
 */
public class DefaultModuleSystem implements PicoModuleSystemSPI {

	private final ModuleMonitor monitor;
	private final Deployer deployer;
	private final FileObject moduleDirectory;
	private final ClassLoader classLoader;
	private MutablePicoContainer parent;
	private final List<MutablePicoContainer> builtContainers = new ArrayList<MutablePicoContainer>();
	private final Set<String> builtContainerNames = new HashSet<String>();
	private final Stack<String> containerRecursionPath = new Stack<String>();

	public DefaultModuleSystem(final ModuleMonitor monitor,
			final Deployer deployer, final FileObject moduleDirectory,
			final ClassLoader classLoader) {
		this.monitor = monitor;
		this.deployer = deployer;
		this.moduleDirectory = moduleDirectory;
		this.classLoader = classLoader;
	}

	/**
	 * @throws MultiException
	 * @throws IllegalStateException
	 * @throws CircularDependencyException
	 */
	public synchronized DefaultModuleSystem deploy(
			final MutablePicoContainer parent) throws MultiException, CircularDependencyException {		
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
		ModuleSystemDeploymentRegistry.setDeployingModule(this);
		try {
			String currentChild;
			for (final FileObject eachChild : moduleDirectory.getChildren()) {
				String moduleName = eachChild.getName().getBaseName();
				currentChild = eachChild.getName().getFriendlyURI();
				try {
					 deployModuleByName(moduleName);
				} catch (final CircularDependencyException e) {
					//Let it through immediately.
					throw e;
				} catch (final RuntimeException ex) {
					errors.addException("Error Deploying Module " + currentChild, ex);
				}
			}
		} catch (final FileSystemException ex) {
			errors.addException(ex);
		} finally {
			ModuleSystemDeploymentRegistry.deploymentComplete();			
		}
		if (errors.getErrorCount() > 0) {
			monitor.multiModuleDeploymentFailed(moduleDirectory, errors);
			throw errors;
		}

		parent.start();
		final long endTime = System.currentTimeMillis();
		monitor.multiModuleDeploymentSuccess(moduleDirectory, returnValue,
				endTime - startTime);
		return this;
	}
	
	private CharSequence getRecursionPathAsString() {
		StringBuilder builder = new StringBuilder();
		boolean needComma = false;
		for (String eachString : containerRecursionPath) {
			if (needComma) {
				builder.append(" -> ");
			}
			needComma = true;
			builder.append(eachString);
		}
		return builder;
	}

	public PicoModuleSystem deployModuleByName(String moduleName) throws RuntimeException {
		if (builtContainerNames.contains(moduleName)) {
			monitor.moduleAlreadyDeployed(moduleName);
			return this;
		}
		
		containerRecursionPath.push(moduleName);
		checkAgainstRecursion(containerRecursionPath);
		try {			
			FileObject moduleFile = moduleDirectory.getChild(moduleName);
			if (!isDeployable(moduleFile)) {
				monitor.skippingDeploymentBecauseNotDeployable(moduleFile);
			} else {

				final MutablePicoContainer result = deployer.deploy(
						moduleFile, this.classLoader, parent, null);
				result.setName(moduleName);
				this.builtContainers.add(result);
			}
		} catch (FileSystemException e) {
			throw new FileSystemRuntimeException("Error deploying module named '" 
					+ moduleName + "'. Deployment path to error: "
					+ getRecursionPathAsString(), e);
		} catch (DeploymentException e) {
			//Let it through.
			throw e;
		} catch (CircularDependencyException e) {
			//Let it through.
			throw e;
		} catch (RuntimeException e) {
			throw new DeploymentException("Error deploying module named '" 
					+ moduleName + "'. Deployment path to error: "
					+ getRecursionPathAsString(), e);
			
		} finally {
			builtContainerNames.add(moduleName);
			containerRecursionPath.pop();
		}
		return this;
	}

	private void checkAgainstRecursion(Stack<String> recursionPath) {
		HashSet<String> allStrings = new HashSet<String>();
		for (String eachString : recursionPath) {
			if (allStrings.contains(eachString)) {
				throw new CircularDependencyException("Circular Dependency detected in modules during deployment.  Path to this recursion: " + getRecursionPathAsString());
			}
			allStrings.add(eachString);
		}
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
