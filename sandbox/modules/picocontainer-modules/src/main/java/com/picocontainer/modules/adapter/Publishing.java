/**
 * 
 */
package com.picocontainer.modules.adapter;

import java.lang.reflect.Type;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;

/**
 * Behavior that allows publishing of services to a parent container while still
 * maintaining references to the child container. This adapter implements a
 * feature similar to exporting in the the OSGi. The basic idea would be that
 * you have a parent container list the exported service, and the child
 * containers (which the parent cannot directly access) contain all the private
 * dependencies that the exported, service relies on.
 * <p>
 * Or in Martin Fowler terms, this adapter allows you to separate Public
 * interfaces (defined in the the child container) from Published interfaces
 * (defined in the parent container)
 * </p>
 * 
 * @author Michael Rimov, Centerline Computers
 */
public class Publishing<T> implements ComponentAdapter<T> {

	private final PicoContainer realDelegatePicoContainer;
	private final ComponentAdapter<T> realComponentAdapter;

	public Publishing(final PicoContainer realDelegatePicoContainer,
			final ComponentAdapter<T> realComponentAdapter) {
		this.realDelegatePicoContainer = realDelegatePicoContainer;
		this.realComponentAdapter = realComponentAdapter;
	}

	public Object getComponentKey() {
		return realComponentAdapter.getComponentKey();
	}

	public Class<? extends T> getComponentImplementation() {
		return realComponentAdapter.getComponentImplementation();
	}

	public T getComponentInstance(final PicoContainer container, final Type into)
			throws PicoCompositionException {
		return realComponentAdapter.getComponentInstance(
				realDelegatePicoContainer, into);
	}

	public void verify(final PicoContainer container)
			throws PicoCompositionException {
		// Does no actual work on itself.
		return;
	}

	public void accept(final PicoVisitor visitor) {
		visitor.visitComponentAdapter(this);
	}

	public ComponentAdapter<T> getDelegate() {
		return realComponentAdapter.getDelegate();
	}

	@SuppressWarnings("rawtypes")
	public <U extends ComponentAdapter> U findAdapterOfType(
			final Class<U> adapterType) {
		return realComponentAdapter.findAdapterOfType(adapterType);
	}

	public String getDescriptor() {
		return "Publishing";
	}

}
