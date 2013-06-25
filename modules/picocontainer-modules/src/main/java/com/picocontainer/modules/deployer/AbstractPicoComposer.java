/**
 * 
 */
package com.picocontainer.modules.deployer;

import com.picocontainer.modules.Publisher;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;

/**
 * 
 * @author Mike
 * 
 */
abstract public class AbstractPicoComposer {

	/**
	 * Creates a child picocontainer.
	 * 
	 * @param parent
	 * @param assemblyScope
	 * @return
	 */
	public MutablePicoContainer createContainer(
			final MutablePicoContainer parent, final ClassLoader cl,
			final Object assemblyScope) {
		final MutablePicoContainer childContainer = constructChildContainer(
				parent, cl, assemblyScope);
		return populateChildContainer(childContainer, parent, assemblyScope);
	}

	/**
	 * Overrideable to create your own container definition.
	 * <p>
	 * Default behavior:
	 * </p>
	 * <ul>
	 * <li>If there is a parent container, then this class uses
	 * {@link com.picocontainer.MutablePicoContainer#makeChildContainer()
	 * makeChildContainer()} so that all parent attributes are propagated to the
	 * child container and lifecycle calls will propagate as well.</li>
	 * <li>If for some reason no parent is supplied (depending on how you use
	 * the deployer system), then we create a default picocontainer with cachine
	 * and lifecycle enabled.</li>
	 * </ul>
	 * 
	 * @param assemblyScope
	 *            Ignored by default, but it allows you to create custom
	 *            containers based on the assembly scope if you use it.
	 * @param assemblyScope2
	 * @return
	 */
	protected MutablePicoContainer constructChildContainer(
			final MutablePicoContainer parent, final ClassLoader cl,
			final Object assemblyScope) {
		if (parent != null) {
			final MutablePicoContainer child = parent.makeChildContainer();
			return child;
		}

		return new PicoBuilder().withCaching().withLifecycle().build();
	}

	/**
	 * Implement to populate the child container.
	 * 
	 * @param childContainer
	 *            the container to populate. Will never be null.
	 * @param parent
	 *            may be null if the current container has no parent.
	 * @param assemblyScope
	 *            Allows for multiple deployment operations in the same script
	 *            if assembly scope is provided. Usage depends on how deployer
	 *            is utilized.
	 * @return the resulting populated mutablepicocontainer. It is usually the
	 *         same one that is passed in as the childContainer parameter, but
	 *         depending on your implementation, you can return whatever you
	 *         wish.
	 */
	abstract protected MutablePicoContainer populateChildContainer(
			MutablePicoContainer childContainer, MutablePicoContainer parent,
			Object assemblyScope);
}
