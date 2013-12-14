/*****************************************************************************
 * Copyright (C) 2003-2013 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Serban Iordache                                          *
 *****************************************************************************/
package com.picocontainer.gems.containers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.Parameter;
import com.picocontainer.PicoException;
import com.picocontainer.containers.AbstractDelegatingMutablePicoContainer;
import com.picocontainer.parameters.ComponentParameter;
import com.picocontainer.parameters.ConstantParameter;
import com.picocontainer.parameters.DefaultConstructorParameter;
import com.picocontainer.parameters.NullParameter;

/**
 * A mutable container that populates itself by means of a configuration file in JSON format.
 *
 * <p/><b>Usage example</b>
 * <br/>Consider the following code fragment:
 * <pre>
 * public class Banner extends JFrame {
 *   public Banner(JTextComponent textComp, String text, int size, boolean bold) {
 *     textComp.setText(text);
 *     textComp.setFont(new Font("Arial", (bold ? Font.BOLD : Font.PLAIN), size));
 *     add(textComp);
 *     setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 *     pack();
 *   }
 *
 *   public static void main(String[] args) {
 *     MutablePicoContainer pico = new DefaultPicoContainer();
 *     pico.addComponent("textComp", JTextArea.class, DefaultConstructorParameter.INSTANCE);
 *     pico.addComponent("banner", Banner.class,
 *         new ComponentParameter("textComp"),
 *         new ConstantParameter("Hello, world!"),
 *         new ComponentParameter("size"),
 *         new ComponentParameter("bold"));
 *     pico.addConfig("size", 36);
 *     pico.addConfig("bold", true);
 *
 *     Banner banner = (Banner)pico.getComponent("banner");
 *     banner.setVisible(true);
 *   }
 * }
 * </pre>
 *
 * In the main method above, the pico container is populated programmatically. Using {@link JsonPicoContainer}, this can be done by means of a JSON configuration file.
 * <br>Content of the file <b>banner.json</b>:<pre>
 * [
 *   {key: textComp, impl: javax.swing.JTextArea, parameters: [{}]},
 *   {key: banner, impl: com.picocontainer.gems.containers.Banner, parameters: [{key: textComp}, {value: "Hello, world"}, {key: size}, {key: bold}]},
 *   {key: size, value: 36, type: int},
 *   {key: bold, value: true, type: boolean}
 * ]
 * </pre>
 *
 * Now, the main method can be rewritten as:
 *
 * <pre>
 *   public static void main(String[] args) {
 *     MutablePicoContainer pico = new JsonPicoContainer("banner.json");
 *
 *     Banner banner = (Banner)pico.getComponent("banner");
 *     banner.setVisible(true);
 *   }
 * </pre>
 *
 * @author Serban Iordache
 */
public class JsonPicoContainer extends AbstractDelegatingMutablePicoContainer {
	private static final long serialVersionUID = 1L;

	private static class KeyOrValue {
		String key;
		String value;
		String type;

		@Override
		public String toString() {
			return "(key: " + key + ", value: " + value + ", type: " + type + ")";
		}
	}

	private static class Component {
		String key;
		String value;
		String type;
		String impl;
		List<KeyOrValue> parameters;

		@Override
		public String toString() {
			return "{key: " + key + ", value: " + value + ", type: " + type + ", impl: " + impl + ", parameters: " + parameters + "}";
		}
	}

	private static class Config extends ArrayList<Component> {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Convenience constructor that creates a JSON container with no parent and populates it with data from a file
	 *
	 * @param jsonFileName - the path to the file containing the json configuration
	 * @throws FileNotFoundException
	 */
	public JsonPicoContainer(String jsonFileName) throws FileNotFoundException {
		this(new FileReader(jsonFileName), null);
	}

	/**
	 * Creates a JSON container with no parent and populates it with data from a file
	 *
	 * @param jsonReader
	 * @param parent
	 */
	public JsonPicoContainer(Reader jsonReader, MutablePicoContainer parent) {
		super(new DefaultPicoContainer(parent));

		MutablePicoContainer pico = getDelegate();
		Gson gson = new Gson();
		Config config;
		try {
			config = gson.fromJson(jsonReader, Config.class);
			for(Component comp : config) {
				if(comp == null) continue;
				if(comp.value == null) {
					if(comp.impl == null) throw new PicoJsonException("Either value or impl must be set in: " + comp);
					Class<?> implClass = Class.forName(comp.impl);
					Parameter[] parameters = null;
					if(comp.parameters != null && !comp.parameters.isEmpty()) {
						int size = comp.parameters.size();
						parameters = new Parameter[size];
						for(int i=0; i<size; i++) {
							parameters[i] = getParameter(comp.parameters.get(i));
						}
					}
					if(comp.key == null) {
						if(parameters != null) throw new PicoJsonException("No parameters are allowed when key=null in: " + comp);
						pico.addComponent(implClass);
					} else {
						pico.addComponent(comp.key, implClass, parameters);
					}
				} else {
					if(comp.impl != null) throw new PicoJsonException("It is not allowed to set both value and impl in: " + comp);
					if(comp.key == null) throw new PicoJsonException("key must be set in: " + comp);
					Object value = getValue(comp.value, comp.type);
					pico.addConfig(comp.key, value);
				}
			}
		} catch(Exception e) {
			if(e instanceof PicoException) throw (PicoException)e;
			else throw new PicoJsonException(e);
		}
	}

	public JsonPicoContainer(Reader jsonReader) {
		this(jsonReader, null);
	}

	@Override
    public void setName(String s) {
        ((DefaultPicoContainer)getDelegate()).setName(s);
    }

	private static Parameter getParameter(KeyOrValue keyOrValue) {
		if(keyOrValue == null) return NullParameter.INSTANCE;
		if(keyOrValue.key == null && keyOrValue.value == null && keyOrValue.type == null) return DefaultConstructorParameter.INSTANCE;
		if(keyOrValue.key == null) {
			if(keyOrValue.value == null) throw new PicoJsonException("Either value or key must be set in: " + keyOrValue);
			Object value = getValue(keyOrValue.value, keyOrValue.type);
			if("class".equals(keyOrValue.type)) return new ComponentParameter(value);
			else return new ConstantParameter(value);

		} else {
			if(keyOrValue.value != null) throw new PicoJsonException("It is not allowed to set both value and key in: " + keyOrValue);
			return new ComponentParameter(keyOrValue.key);
		}
	}

	private static Object getValue(String valueAsString, String type) {
		if(type == null) type = "string";
		if("string".equals(type)) return valueAsString;
		if("int".equals(type)) return Integer.valueOf(valueAsString);
		if("long".equals(type)) return Long.valueOf(valueAsString);
		if("float".equals(type)) return Float.valueOf(valueAsString);
		if("double".equals(type)) return Double.valueOf(valueAsString);
		if("boolean".equals(type)) return Boolean.valueOf(valueAsString);
		if("class".equals(type)) {
			try {
				return Class.forName(valueAsString);
			} catch(ClassNotFoundException e) {
				throw new PicoJsonException(e);
			}
		}
		throw new PicoJsonException("Unsupported type: " + type + " for value: " + valueAsString);
	}


    @Override
    public String toString() {
        return "[Json]:" + getDelegate().toString();
    }

	@Override
	public MutablePicoContainer makeChildContainer() {
		return getDelegate().makeChildContainer();
	}
}
