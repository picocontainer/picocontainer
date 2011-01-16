package org.picocontainer.modules;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.adapter.Publishing;

/**
 * Quick utility class to allow one method publishing.  Example:
 * <pre>
 *   MutablePicoContainer parent = new PicoBuilder()
 *   					.withCaching()
 *   					.withLifecycle()
 *   					.build();
 *   
 *   MutablePicoContainer child =  [ Repeat as parent ]
 *
 *	child.addComponent(Key1.class)
 *		.addComponent(Key2.class)
 *		.addComponent(Key3.class);
 *
 *  assertNull(parent.getComponent(Key1.class);
 *  //Now Publish Desired components to parent
 *  new Publisher(child, parent)
 *  	.publish(Key2.class)
 *  	.publish(Key3.class);
 *  
 *  assertNotNull(parent.getComponent(Key1.class);
 * </pre>
 * @author Michael Rimov, Centerline Computers, Inc.
 */
public class Publisher {

	private final MutablePicoContainer parent;
	private final MutablePicoContainer child;

	public Publisher(MutablePicoContainer child, MutablePicoContainer parent) {
		this.parent = parent;
		this.child = child;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Publisher publish(Object key) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		final ComponentAdapter ca = child.getComponentAdapter(key);
		if (ca == null) {
			throw new IllegalArgumentException(key + " does not appear to exist in container " + child);
		}
		parent.addAdapter(new Publishing<Object>(child, ca));
		return this;
	}
}
