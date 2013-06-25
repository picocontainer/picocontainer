package com.picocontainer.script.jruby;

import java.util.Properties;

import org.jruby.RubyClass;
import com.picocontainer.script.util.ComponentElementHelper;

import com.picocontainer.Parameter;
import com.picocontainer.classname.ClassLoadingPicoContainer;

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
	public static Object makeComponent(final Object classNamekey, final Object key, final Parameter[] parameters, final Object classValue, final ClassLoadingPicoContainer current, final Object instance, final Properties[] properties) {
		Object classValueToUse = classValue;
		if (classValueToUse != null && classValueToUse instanceof RubyClass) {
			classValueToUse = ((RubyClass)classValueToUse).getReifiedClass();
		}

		return ComponentElementHelper.makeComponent(classNamekey, key, parameters, classValueToUse, current,  instance, properties);
	}

	 public static Object makeComponent(final Object classNameKey,
             final Object key,
             final Parameter[] parameters,
             final Object classValue,
             final ClassLoadingPicoContainer container, final Object instance) {
		 return makeComponent(classNameKey, key, parameters, classValue, container, instance, new Properties[0]);
	 }

}
