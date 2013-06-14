package org.picocontainer.script.jruby;

import java.util.Properties;

import org.jruby.RubyClass;
import org.picocontainer.Parameter;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.script.util.ComponentElementHelper;

public class JRubyComponentElementHelper {

	private JRubyComponentElementHelper() {
	}
	
	/**
	 * Reifies jruby classes to java classes before passing to to ComponentElementHelper
	 * @param classNamekey
	 * @param key
	 * @param parameters
	 * @param classValue
	 * @param current
	 * @param instance
	 * @param properties
	 * @return
	 */
	public static Object makeComponent(Object classNamekey, Object key, Parameter[] parameters, Object classValue, ClassLoadingPicoContainer current, Object instance, Properties[] properties) {
		Object classValueToUse = classValue;
		if (classValueToUse != null && classValueToUse instanceof RubyClass) {
			classValueToUse = ((RubyClass)classValueToUse).getReifiedClass();
		}
		
		return ComponentElementHelper.makeComponent(classNamekey, key, parameters, classValueToUse, current,  instance, properties);
	}
	
	 public static Object makeComponent(Object classNameKey,
             Object key,
             Parameter[] parameters,
             Object classValue,
             ClassLoadingPicoContainer container, Object instance) {
		 return makeComponent(classNameKey, key, parameters, classValue, container, instance, new Properties[0]);
	 }	

}
