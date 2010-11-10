/**
 * 
 */
package org.picocontainer.modules.adapter;

import java.lang.reflect.Type;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * Behavior that allows exporting of services to a parent container while
 * still maintaining references to the child container.  This adapter implements a feature similar
 * to exporting in the the OSGi.   The basic idea would be that you have a parent container list the
 * exported service, and the child containers (which the parent cannot directly access) contain
 * all the private dependencies that the exported, service relies on.
 * @author Michael Rimov, Centerline Computers
 */
public class Exporting<T> implements ComponentAdapter<T> {
	
	private final PicoContainer realDelegatePicoContainer;
	private final ComponentAdapter<T> realComponentAdapter;

	public Exporting(PicoContainer realDelegatePicoContainer, ComponentAdapter<T> realComponentAdapter) {
		this.realDelegatePicoContainer = realDelegatePicoContainer;
		this.realComponentAdapter = realComponentAdapter;		
	}

	public Object getComponentKey() {
		return realComponentAdapter.getComponentKey();
	}

	public Class<? extends T> getComponentImplementation() {
		return realComponentAdapter.getComponentImplementation();
	}

	public T getComponentInstance(PicoContainer container, Type into)
			throws PicoCompositionException {
		return realComponentAdapter.getComponentInstance(realDelegatePicoContainer, into);
	}

	public void verify(PicoContainer container) throws PicoCompositionException {
		//Does no actual work on itself.
		return;		
	}

	public void accept(PicoVisitor visitor) {
		visitor.visitComponentAdapter(this);
	}

	public ComponentAdapter<T> getDelegate() {
		return realComponentAdapter.getDelegate();
	}

	public <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
		return realComponentAdapter.findAdapterOfType(adapterType);
	}

	public String getDescriptor() {
		return "Promoting";
	}

}
