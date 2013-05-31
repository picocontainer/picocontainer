/**
 * 
 */
package org.picocontainer.parameters;

import java.util.List;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.JTypeHelper;
import org.picocontainer.Parameter;

import com.googlecode.jtype.Generic;

/**
 * Extension to ComponentParameter that attempts to sort out "ambiguous components" in a way compatible
 * with JSR330.  Currently if there is a {@link javax.inject.Named} annotation or a qualifier, they are
 * registered with a string key while default components (no qualifier) will have the key of the implementation
 * of the class (usually).  
 * <p>If we get a situation where AmbiguousComponentResolution would be thrown, then we try this sorting it out algorithm
 * first.</p>
 * 
 * @author Michael Rimov
 *
 */
@SuppressWarnings("serial")
public class JSR330ComponentParameter extends ComponentParameter {
	
	public static final JSR330ComponentParameter DEFAULT = new JSR330ComponentParameter();

	/**
	 * @param key
	 */
	public JSR330ComponentParameter(Object key) {
		super(key);
	}


	/**
	 * 
	 */
	public JSR330ComponentParameter() {
	}

	/**
	 * @param emptyCollection
	 */
	public JSR330ComponentParameter(boolean emptyCollection) {
		super(emptyCollection);
	}


	/**
	 * @param componentValueType
	 * @param emptyCollection
	 */
	public JSR330ComponentParameter(Generic<?> componentValueType, boolean emptyCollection) {
		super(componentValueType, emptyCollection);
	}

	/**
	 * @param keyType
	 * @param componentValueType
	 * @param emptyCollection
	 */
	public JSR330ComponentParameter(Class<?> keyType, Generic<?> componentValueType, boolean emptyCollection) {
		super(keyType, componentValueType, emptyCollection);
	}

	/**
	 * @param mapDefiningParameter
	 */
	public JSR330ComponentParameter(Parameter mapDefiningParameter) {
		super(mapDefiningParameter);
	}


	/**
	 * Override that looks to see if there is only one component adapter with a class as the
	 * key since Providers and qualifiers will automatically have a string value.
	 */
	@Override
	protected <T> ComponentAdapter<T> sortThroughTooManyAdapters(Generic<T> expectedType,
			List<ComponentAdapter<T>> found) {
		ComponentAdapter<T> lastAdapterWithClassKey = null;
		for (ComponentAdapter<T> eachAdapter : found) {
			if (eachAdapter.getComponentKey() instanceof Class<?>) {
				//More than one found, bail.
				if (lastAdapterWithClassKey != null) {
					lastAdapterWithClassKey = null;
					break;
				}
				
				lastAdapterWithClassKey = eachAdapter;
			}
		}
		
		if (lastAdapterWithClassKey == null) {
			lastAdapterWithClassKey = checkForMatchingGenericParameterTypes(expectedType, found);
		}
		return lastAdapterWithClassKey;
	}
	
	protected <T> ComponentAdapter<T> checkForMatchingGenericParameterTypes(Generic<T> expectedType, List<ComponentAdapter<T>> found) {

		ComponentAdapter<T> lastFound = null;
		for (ComponentAdapter<T> eachCA : found) {
			if (JTypeHelper.isAssignableFrom(expectedType, eachCA.getComponentImplementation())) {
				//More than one found, bail.
				if (lastFound != null) {
					lastFound = null;
					break;
				}
				
				lastFound = eachCA;
			}
			
		}
		
		return lastFound;
	}

}
