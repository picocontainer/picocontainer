/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.remoting;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.picocontainer.web.DELETE;
import com.picocontainer.web.GET;
import com.picocontainer.web.NONE;
import com.picocontainer.web.POST;
import com.picocontainer.web.PUT;
import com.picocontainer.web.PicoContainerWebException;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.injectors.MethodInjection;
import com.picocontainer.injectors.MultiArgMemberInjector;
import com.picocontainer.injectors.ProviderAdapter;
import com.picocontainer.injectors.Reinjector;
import com.picocontainer.monitors.NullComponentMonitor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;

/**
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class PicoWebRemoting {

	private static final String SLASH = "/";
	private static final String DOT = ".";
	private static final String OK = "OK";
	private static final String NL = "\n";
	private static final String GET = "GET";
	private static final String PUT = "PUT";
	private static final String DELETE = "DELETE";
	private static final String POST = "POST";
	private static final String FALLBACK = "FALLBACK";

	private final XStream xstream;
	private final String toStripFromUrls;
	private final String suffixToStrip;
	private final String scopesToPublish;
	private final boolean lowerCasePath;
	private final boolean useMethodNamePrefixesForVerbs;
	private PicoWebRemotingMonitor monitor;

	private Map<String, Object> paths = new HashMap<String, Object>();

	public PicoWebRemoting(XStream xstream, String prefixToStripFromUrls,
			String suffixToStrip, String scopesToPublish,
			boolean lowerCasePath, boolean useMethodNamePrefixesForVerbs) {
		this(xstream, prefixToStripFromUrls, suffixToStrip, scopesToPublish,
				lowerCasePath, useMethodNamePrefixesForVerbs,
				new NullPicoWebRemotingMonitor());
	}

	public PicoWebRemoting(XStream xstream, String prefixToStripFromUrls,
			String suffixToStrip, String scopesToPublish,
			boolean lowerCasePath, boolean useMethodNamePrefixesForVerbs,
			PicoWebRemotingMonitor monitor) {
		this.xstream = xstream;
		this.toStripFromUrls = prefixToStripFromUrls;
		this.suffixToStrip = suffixToStrip;
		this.scopesToPublish = scopesToPublish;
		this.lowerCasePath = lowerCasePath;
		this.useMethodNamePrefixesForVerbs = useMethodNamePrefixesForVerbs;
		this.monitor = monitor;
		this.xstream.registerConverter(new ISO8601DateConverter());
	}

	public Map<String, Object> getPaths() {
		return paths;
	}

	public void setMonitor(PicoWebRemotingMonitor monitor) {
		this.monitor = monitor;
	}

	protected String processRequest(String pathInfo,
			PicoContainer reqContainer, String httpMethod,
			NullComponentMonitor monitor) throws IOException {
		try {
		    if (pathInfo == null || pathInfo.length() == 0) {
		        throw makeNothingMatchingException();
		    }
			String path = pathInfo.substring(1);
			if (path.endsWith(SLASH)) {
				path = path.substring(0, path.length() - 1);
			}
			path = toStripFromUrls + path;

			if (suffixToStrip != null && path.endsWith(suffixToStrip)) {
				path = path.substring(0, path.indexOf(suffixToStrip));
			}

			long b4 = System.currentTimeMillis();
			Object node = getNode(reqContainer, httpMethod, path, monitor);
			long dur = System.currentTimeMillis() - b4;

			if (node instanceof Directories) {
				Directories directories = (Directories) node;
				return xstream.toXML(sortedSet(directories).toArray()) + NL;
			} else if (node instanceof WebMethods) {
				WebMethods methods = (WebMethods) node;
				return xstream.toXML(sortedSet(methods.keySet()).toArray())
						+ NL;
			} else if (node != null && isComposite(node)) {
				b4 = System.currentTimeMillis();
				String s = xstream.toXML(node) + NL;
				dur = System.currentTimeMillis() - b4;
				return s;
			} else if (node != null) {
				return node != null ? xstream.toXML(node) + NL : null;
			} else {
				throw makeNothingMatchingException();
			}

		} catch (MultiArgMemberInjector.ParameterCannotBeNullException e) {
			return errorResult(this.monitor.nullParameterForMethodInvocation(e
					.getParameterName()));
		} catch (PicoCompositionException e) {
			return errorResult(this.monitor
					.picoCompositionExceptionForMethodInvocation(e));
		} catch (RuntimeException e) {
			Object o = this.monitor.runtimeExceptionForMethodInvocation(e);
			return errorResult(o);
		}

	}

	protected SortedSet<String> sortedSet(Set<String> set) {
		return new TreeSet<String>(set);
	}

	private RuntimeException makeNothingMatchingException() {
		return new PicoContainerWebException(
				"Nothing matches the path requested");
	}

	private Object getNode(PicoContainer reqContainer, String httpMethod,
			String path, NullComponentMonitor monitor) throws IOException {
		Object node = paths.get(path);

		if (node == null) {
			int ix = path.lastIndexOf(SLASH);
			if (ix > 0) {
				String methodName = path.substring(ix + 1);
				path = path.substring(0, ix);
				Object node2 = paths.get(path);
				if (node2 instanceof WebMethods) {
					node = processWebMethodRequest(reqContainer, httpMethod,
							methodName, node2, monitor);
				}
			} else {
				node = null;
			}
		}
		return node;
	}

	private Object processWebMethodRequest(PicoContainer reqContainer,
			String verb, String methodName, Object node2,
			NullComponentMonitor monitor) throws IOException {
		WebMethods methods = (WebMethods) node2;
		if (!methods.containsKey(methodName)) {
			throw makeNothingMatchingException();
		}
		HashMap<String, Method> methodz = methods.get(methodName);
		Method method = null;
		if (useMethodNamePrefixesForVerbs) {
			method = methodz.get(verb);
		}
		if (method == null) {
			method = methodz.get(FALLBACK);
		}
		if (method == null) {
			throw new PicoContainerWebException("method not allowed for "
					+ verb);
		}
		return reinject(methodName, method, methods.getKey(),
				methods.getImpl(), reqContainer, monitor);
	}

	private boolean delete(Method method) {
		return method.getAnnotation(DELETE.class) != null;
	}

	private boolean put(Method method) {
		return method.getAnnotation(PUT.class) != null;
	}

	private boolean get(Method method) {
		return method.getAnnotation(GET.class) != null;
	}

	private boolean post(Method method) {
		return method.getAnnotation(POST.class) != null;
	}

	protected void publishAdapters(Collection<ComponentAdapter<?>> adapters,
			String scope) {
		if (scopesToPublish == null || scopesToPublish.contains(scope)) {
			for (ComponentAdapter<?> ca : adapters) {
				Object key = ca.getComponentKey();
				if (notAProvider(ca) && notServletMechanics(key)
						&& keyIsAType(key)) {
					publishAdapter(ca);
				}
			}
		}
	}

	private boolean notAProvider(ComponentAdapter<?> ca) {
		return ca.findAdapterOfType(ProviderAdapter.class) == null;
	}

	private boolean keyIsAType(Object key) {
		return key instanceof Class;
	}

	protected boolean notServletMechanics(Object key) {
		return key != HttpSession.class && key != HttpServletRequest.class
				&& key != HttpServletResponse.class;
	}

	private void determineEligibleMethods(Class<?> component,
			WebMethods webMethods) {
		Method[] methods = component.getDeclaredMethods();
		for (Method method : methods) {
			if (Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers())
					&& method.getAnnotation(NONE.class) == null) {
				String webMethodName = getMethodName(method);
				String webVerb = getVerbName(method);
				HashMap<String, Method> methodz = webMethods.get(webMethodName);
				if (methodz == null) {
					methodz = new HashMap<String, Method>();
					webMethods.put(webMethodName, methodz);
				}
				if (post(method)) {
					methodz.put(POST, method);
				}
				if (get(method)) {
					methodz.put(GET, method);
				}
				if (delete(method)) {
					methodz.put(DELETE, method);
				}
				if (put(method)) {
					methodz.put(PUT, method);
				}
				methodz.put(webVerb, method);
			}
		}
		Class<?> superClass = component.getSuperclass();
		if (superClass != null && superClass != Object.class) {
			determineEligibleMethods(superClass, webMethods);
		}
	}

	private String getMethodName(Method method) {
		String name = method.getName();
		if (!useMethodNamePrefixesForVerbs) {
			return name;
		}
		if (prefixFolledByUpperChar(name, GET.toLowerCase())) {
			return name.substring(3, 4).toLowerCase() + name.substring(4);
		} else if (prefixFolledByUpperChar(name, PUT.toLowerCase())) {
			return name.substring(3, 4).toLowerCase() + name.substring(4);
		} else if (prefixFolledByUpperChar(name, DELETE.toLowerCase())) {
			return name.substring(6, 7).toLowerCase() + name.substring(7);
		} else if (prefixFolledByUpperChar(name, POST.toLowerCase())) {
			return name.substring(4, 5).toLowerCase() + name.substring(5);
		} else {
			return name;
		}
	}

	private String getVerbName(Method method) {
		if (!useMethodNamePrefixesForVerbs) {
			return FALLBACK;
		}
		String name = method.getName();
		if (prefixFolledByUpperChar(name, GET.toLowerCase())) {
			return GET;
		} else if (prefixFolledByUpperChar(name, PUT.toLowerCase())) {
			return PUT;
		} else if (prefixFolledByUpperChar(name, DELETE.toLowerCase())) {
			return DELETE;
		} else if (prefixFolledByUpperChar(name, POST.toLowerCase())) {
			return POST;
		} else {
			return FALLBACK;
		}
	}

	private boolean prefixFolledByUpperChar(String name, String prefix) {
		return name.startsWith(prefix) && name.length() > prefix.length()
				&& Character.isUpperCase(name.charAt(prefix.length()));
	}

	private void publishAdapter(ComponentAdapter<?> ca) {
		Class<?> key = (Class<?>) ca.getComponentKey();
		String path = getClassName(key).replace(DOT, SLASH);
		if (toStripFromUrls != "" || path.startsWith(toStripFromUrls)) {
			paths.put(path, key);
			directorize(path, key, ca.getComponentImplementation());
			directorize(path);
		}
	}

	private String getClassName(Class<?> key) {
		String name = key.getName();
		if (lowerCasePath) {
			return name.toLowerCase();
		} else {
			return name;
		}
	}

	protected void directorize(String path, Class<?> key,
			Class<?> impl) {
		WebMethods webMethods = new WebMethods(key, impl);
		paths.put(path, webMethods);
		determineEligibleMethods(impl, webMethods);
	}

	private String errorResult(Object errorResult) {
		return xstream.toXML(errorResult) + NL;
	}

	private boolean isComposite(Object node) {
		return !(node.getClass().isPrimitive() || node instanceof Boolean
				|| node instanceof Long || node instanceof Double
				|| node instanceof Short || node instanceof Byte
				|| node instanceof Integer || node instanceof String
				|| node instanceof Float || node instanceof Character);
	}

	private Object reinject(String methodName, Method method, Class<?> key,
			Class<?> impl, PicoContainer reqContainer,
			NullComponentMonitor monitor) throws IOException {
		MethodInjection methodInjection = new MethodInjection(method);
		Reinjector reinjector = new Reinjector(reqContainer, monitor);

		Properties props = (Properties) Characteristics.USE_NAMES.clone();
		Object inst = reqContainer.getComponent(key);
		Object rv = reinjector
				.reinject(key, impl, inst, props, methodInjection);
		if (method.getReturnType() == void.class) {
			return OK;
		}
		return rv;
	}

	@SuppressWarnings("unchecked")
	protected void directorize(String path) {
		int lastSlashIx = path.lastIndexOf(SLASH);
		if (lastSlashIx != -1) {
			String dir = path.substring(0, lastSlashIx);
			String file = path.substring(lastSlashIx + 1);
			Set<String> dirs = (Set<String>) paths.get(dir);
			if (dirs == null) {
				dirs = new Directories();
				paths.put(dir, dirs);
			}
			dirs.add(file);
			directorize(dir);
		} else {
			Set<String> dirs = (Set<String>) paths.get(SLASH);
			if (dirs == null) {
				dirs = new Directories();
				paths.put("", dirs);
			}
			dirs.add(path);
		}
	}

	public void visitClass(String clazz,
			MutablePicoContainer mutablePicoContainer, MethodVisitor mapv)
			throws IOException {
		String s = toStripFromUrls + clazz;
		Object node = paths.get(s);
		if (node instanceof WebMethods) {
			WebMethods wm = (WebMethods) node;
			Class<?> x = wm.getKey();
			Class<?> y = x.getSuperclass();
			if (y != null) {
				String s1 = y.getName().replace(DOT, SLASH);
				if (s1.startsWith(toStripFromUrls)) {
					mapv.superClass(s1.substring(toStripFromUrls.length()));
				} else {
					mapv.superClass(s1);
				}
			}
			Set<?> keys = sortedSet(wm.keySet());
			for (Object o : keys) {
				String methodName = o.toString();
				HashMap<String, Method> foo = wm.get(methodName);
				Method m = foo.get(GET);
				if (m == null) {
					m = foo.get(FALLBACK);
				}
				mapv.method(methodName, m);
			}
		}
	}

	@SuppressWarnings("serial")
	protected static class Directories extends HashSet<String> {
	}

	@SuppressWarnings("serial")
	public static class WebMethods extends
			HashMap<String, HashMap<String, Method>> {
		private final Class<?> key;
		private final Class<?> impl;

		public WebMethods(Class<?> key, Class<?> impl) {
			this.key = key;
			this.impl = impl;
		}

		public Class<?> getKey() {
			return key;
		}

		public Class<?> getImpl() {
			return impl;
		}
	}
}
