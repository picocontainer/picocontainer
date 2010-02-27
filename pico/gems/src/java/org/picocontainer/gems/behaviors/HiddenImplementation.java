/**
 * 
 */
package org.picocontainer.gems.behaviors;

import org.picocontainer.ComponentAdapter;

/**
 * Backwards Compatibility stub for the renamed AsmHiddenImplementation.
 * @author Michael Rimov
 * @deprecated  Use AsmHiddenImplementation instead.
 * @since PicoContainer 2.4
 */
@Deprecated
@SuppressWarnings("serial")
public class HiddenImplementation<T> extends AsmHiddenImplementation<T> {


	/**
	 * @param delegate
	 */
	public HiddenImplementation(ComponentAdapter<T> delegate) {
		super(delegate);
	}



}
