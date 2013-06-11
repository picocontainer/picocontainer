/**
 * 
 */
package org.picocontainer.injectors;

import java.lang.reflect.Type;

import org.picocontainer.Injector;
import org.picocontainer.PicoContainer;

/**
 * Performs injection into static members of a class and does not return an instance.
 * @author Michael Rimov
 *
 */
public interface StaticInjector<T> extends Injector<T> {
	
	void injectStatics(final PicoContainer container, final Type into);
	
}
