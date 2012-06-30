/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.parameters;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.JTypeHelper;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import com.googlecode.jtype.Generic;

/**
 * A CollectionComponentParameter should be used to support inject an
 * {@link Array}, a {@link Collection}or {@link Map}of components automatically.
 * The collection will contain all components of a special type and additionally
 * the type of the key may be specified. In case of a map, the map's keys are
 * the one of the component adapter.
 * 
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class CollectionComponentParameter extends AbstractParameter implements Parameter, Serializable {

	/**
	 * Use <code>ARRAY</code> as {@link Parameter}for an Array that must have
	 * elements.
	 */
	public static final CollectionComponentParameter ARRAY = new CollectionComponentParameter();
	/**
	 * Use <code>ARRAY_ALLOW_EMPTY</code> as {@link Parameter}for an Array that
	 * may have no elements.
	 */
	public static final CollectionComponentParameter ARRAY_ALLOW_EMPTY = new CollectionComponentParameter(true);

	private final boolean emptyCollection;
	private final Class keyType;
	private final Generic<?> componentValueType;

	/**
	 * Expect an {@link Array}of an appropriate type as parameter. At least one
	 * component of the array's component type must exist.
	 */
	public CollectionComponentParameter() {
		this(false);
	}

	/**
	 * Expect an {@link Array}of an appropriate type as parameter.
	 * 
	 * @param emptyCollection
	 *            <code>true</code> if an empty array also is a valid dependency
	 *            resolution.
	 */
	public CollectionComponentParameter(final boolean emptyCollection) {
		this(JTypeHelper.VOID, emptyCollection);
	}

	/**
	 * Expect any of the collection types {@link Array},{@link Collection}or
	 * {@link Map}as parameter.
	 * 
	 * @param componentValueType
	 *            the type of the components (ignored in case of an Array)
	 * @param emptyCollection
	 *            <code>true</code> if an empty collection resolves the
	 */
	public CollectionComponentParameter(final Generic<?> componentValueType, final boolean emptyCollection) {
		this(Object.class, componentValueType, emptyCollection);
	}

	/**
	 * Expect any of the collection types {@link Array},{@link Collection}or
	 * {@link Map}as parameter.
	 * 
	 * @param keyType
	 *            the type of the component's key
	 * @param componentValueType
	 *            the type of the components (ignored in case of an Array)
	 * @param emptyCollection
	 *            <code>true</code> if an empty collection resolves the
	 */
	public CollectionComponentParameter(final Class keyType, final Generic<?> componentValueType,
			final boolean emptyCollection) {
		this.emptyCollection = emptyCollection;
		this.keyType = keyType;
		this.componentValueType = componentValueType;
	}

	/**
	 * Check for a successful dependency resolution of the parameter for the
	 * expected type. The dependency can only be satisfied if the expected type
	 * is one of the collection types {@link Array},{@link Collection}or
	 * {@link Map}. An empty collection is only a valid resolution, if the
	 * <code>emptyCollection</code> flag was set.
	 * 
	 * @param container
	 *            {@inheritDoc}
	 * @param injecteeAdapter
	 * @param expectedType
	 *            {@inheritDoc}
	 * @param expectedNameBinding
	 *            {@inheritDoc}
	 * @param useNames
	 * @param binding
	 *            @return <code>true</code> if matching components were found or
	 *            an empty collective type is allowed
	 */
	public Resolver resolve(final PicoContainer container, final ComponentAdapter<?> forAdapter,
			final ComponentAdapter<?> injecteeAdapter, final Type expectedType, final NameBinding expectedNameBinding,
			final boolean useNames, final Annotation binding) {
		final Class collectionType = getCollectionType(expectedType);
		if (collectionType != null) {
			final Map<Object, ComponentAdapter<?>> componentAdapters = getMatchingComponentAdapters(container,
					forAdapter, keyType, getValueType(expectedType));
			return new Resolver() {
				public boolean isResolved() {
					return emptyCollection || componentAdapters.size() > 0;
				}

				public Object resolveInstance(final Type into) {
					Object result = null;
					if (collectionType.isArray()) {
						result = getArrayInstance(container, collectionType, componentAdapters, into);
					} else if (Map.class.isAssignableFrom(collectionType)) {
						result = getMapInstance(container, collectionType, componentAdapters, into);
					} else if (Collection.class.isAssignableFrom(collectionType)) {
						result = getCollectionInstance(container, collectionType, componentAdapters,
								expectedNameBinding, useNames, into);
					} else {
						throw new PicoCompositionException(expectedType + " is not a collective type");
					}
					return result;
				}

				public ComponentAdapter<?> getComponentAdapter() {
					return null;
				}
			};
		}
		return new Parameter.NotResolved();
	}

	public Class getCollectionType(final Type expectedType) {
		if (expectedType instanceof Class) {
			return getCollectionType((Class) expectedType);
		} else if (expectedType instanceof ParameterizedType) {
			final ParameterizedType type = (ParameterizedType) expectedType;

			return getCollectionType(type.getRawType());
		} else if (expectedType instanceof GenericArrayType) {
//			return getCollectionType(((GenericArrayType) expectedType).getGenericComponentType());
	          GenericArrayType type = (GenericArrayType) expectedType;
	          Class baseType = getGenericArrayBaseType(type.getGenericComponentType());
	          return Array.newInstance(baseType, 0).getClass();
	    }

		throw new IllegalArgumentException("Unable to get collection type from " + expectedType);
	}
	
	   private Class getGenericArrayBaseType(final Type expectedType) {
	        if (expectedType instanceof Class) {
	            Class type = (Class) expectedType;
	            return type;
	        }
	        else if (expectedType instanceof ParameterizedType) {
	            ParameterizedType type = (ParameterizedType) expectedType;
	            return getGenericArrayBaseType(type.getRawType());
	        }

	        throw new IllegalArgumentException("Unable to get collection type from " + expectedType);
	    }	

	/**
	 * Verify a successful dependency resolution of the parameter for the
	 * expected type. The method will only return if the expected type is one of
	 * the collection types {@link Array}, {@link Collection}or {@link Map}. An
	 * empty collection is only a valid resolution, if the
	 * <code>emptyCollection</code> flag was set.
	 * 
	 * @param container
	 *            {@inheritDoc}
	 * @param adapter
	 *            {@inheritDoc}
	 * @param expectedType
	 *            {@inheritDoc}
	 * @param expectedNameBinding
	 *            {@inheritDoc}
	 * @param useNames
	 * @param binding
	 * @throws PicoCompositionException
	 *             {@inheritDoc}
	 */
	public void verify(final PicoContainer container, final ComponentAdapter<?> adapter, final Type expectedType,
			final NameBinding expectedNameBinding, final boolean useNames, final Annotation binding) {
		final Class collectionType = getCollectionType(expectedType);
		if (collectionType != null) {
			final Generic<?> valueType = getValueType(expectedType);
			final Collection componentAdapters = getMatchingComponentAdapters(container, adapter, keyType, valueType)
					.values();
			if (componentAdapters.isEmpty()) {
				if (!emptyCollection) {
					throw new PicoCompositionException(expectedType + " not resolvable, no components of type "
							+ valueType.toString() + " available");
				}
			} else {
				for (final Object componentAdapter1 : componentAdapters) {
					final ComponentAdapter componentAdapter = (ComponentAdapter) componentAdapter1;
					componentAdapter.verify(container);
				}
			}
		} else {
			throw new PicoCompositionException(expectedType + " is not a collective type");
		}
	}

	/**
	 * Visit the current {@link Parameter}.
	 * 
	 * @see org.picocontainer.Parameter#accept(org.picocontainer.PicoVisitor)
	 */
	public void accept(final PicoVisitor visitor) {
		visitor.visitParameter(this);
	}

	/**
	 * Evaluate whether the given component adapter will be part of the
	 * collective type.
	 * 
	 * @param adapter
	 *            a <code>ComponentAdapter</code> value
	 * @return <code>true</code> if the adapter takes part
	 */
	protected boolean evaluate(final ComponentAdapter adapter) {
		return adapter != null; // use parameter, prevent compiler warning
	}

	/**
	 * Collect the matching ComponentAdapter instances.
	 * 
	 * @param container
	 *            container to use for dependency resolution
	 * @param adapter
	 *            {@link org.picocontainer.ComponentAdapter} to exclude
	 * @param keyType
	 *            the compatible type of the key
	 * @param valueType
	 *            the compatible type of the addComponent
	 * @return a {@link Map} with the ComponentAdapter instances and their
	 *         component keys as map key.
	 */
	@SuppressWarnings({ "unchecked" })
	protected Map<Object, ComponentAdapter<?>> getMatchingComponentAdapters(final PicoContainer container,
			final ComponentAdapter adapter, final Class keyType, final Generic<?> valueType) {
		final Map<Object, ComponentAdapter<?>> adapterMap = new LinkedHashMap<Object, ComponentAdapter<?>>();
		final PicoContainer parent = container.getParent();
		if (parent != null) {
			adapterMap.putAll(getMatchingComponentAdapters(parent, adapter, keyType, valueType));
		}
		final Collection<ComponentAdapter<?>> allAdapters = container.getComponentAdapters();
		for (final ComponentAdapter componentAdapter : allAdapters) {
			adapterMap.remove(componentAdapter.getComponentKey());
		}
		final List<ComponentAdapter> adapterList = List.class.cast(container.getComponentAdapters(valueType));
		for (final ComponentAdapter componentAdapter : adapterList) {
			final Object key = componentAdapter.getComponentKey();
			if (adapter != null && key.equals(adapter.getComponentKey())) {
				continue;
			}
			if (keyType.isAssignableFrom(key.getClass()) && evaluate(componentAdapter)) {
				adapterMap.put(key, componentAdapter);
			}
		}
		return adapterMap;
	}

	private Class getCollectionType(final Class collectionType) {
		if (collectionType.isArray() || Map.class.isAssignableFrom(collectionType)
				|| Collection.class.isAssignableFrom(collectionType)) {
			return collectionType;
		}

		return null;
	}

	private Generic<?> getValueType(final Type collectionType) {
		if (collectionType instanceof Class) {
			return getValueType((Class) collectionType);
		} else if (collectionType instanceof ParameterizedType) {
			return getValueType((ParameterizedType) collectionType);
		} else if (collectionType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) collectionType;
            
            return Generic.get(getGenericArrayBaseType(genericArrayType.getGenericComponentType()));
        }
		throw new IllegalArgumentException("Unable to determine collection type from " + collectionType);
	}

	private Generic<?> getValueType(final Class collectionType) {
		Generic<?> valueType = componentValueType;
		if (collectionType.isArray()) {
			valueType = Generic.get(collectionType.getComponentType());
		}
		return valueType;
	}

	private Generic<?> getValueType(final ParameterizedType collectionType) {
		Generic<?> valueType = componentValueType;
		if (Collection.class.isAssignableFrom((Class<?>) collectionType.getRawType())) {
			final Type type = collectionType.getActualTypeArguments()[0];
			if (type instanceof Class) {
				if (JTypeHelper.isAssignableTo(valueType, (Class) type)) {
					return valueType;
				}
				valueType = Generic.get((Class) type);
			}
		}
		return valueType;
	}

	private Object[] getArrayInstance(final PicoContainer container, final Class expectedType,
			final Map<Object, ComponentAdapter<?>> adapterList, final Type into) {
		final Object[] result = (Object[]) Array.newInstance(expectedType.getComponentType(), adapterList.size());
		int i = 0;
		for (final ComponentAdapter componentAdapter : adapterList.values()) {
			result[i] = container.getComponentInto(componentAdapter.getComponentKey(), into);
			i++;
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	private Collection getCollectionInstance(final PicoContainer container,
			final Class<? extends Collection> expectedType, final Map<Object, ComponentAdapter<?>> adapterList,
			final NameBinding expectedNameBinding, final boolean useNames, final Type into) {
		Class<? extends Collection> collectionType = expectedType;
		if (collectionType.isInterface()) {
			// The order of tests are significant. The least generic types last.
			if (List.class.isAssignableFrom(collectionType)) {
				collectionType = ArrayList.class;
				// } else if
				// (BlockingQueue.class.isAssignableFrom(collectionType)) {
				// collectionType = ArrayBlockingQueue.class;
				// } else if (Queue.class.isAssignableFrom(collectionType)) {
				// collectionType = LinkedList.class;
			} else if (SortedSet.class.isAssignableFrom(collectionType)) {
				collectionType = TreeSet.class;
			} else if (Set.class.isAssignableFrom(collectionType)) {
				collectionType = HashSet.class;
			} else if (Collection.class.isAssignableFrom(collectionType)) {
				collectionType = ArrayList.class;
			}
		}
		try {
			final Collection result = collectionType.newInstance();
			for (final ComponentAdapter componentAdapter : adapterList.values()) {
				if (!useNames || componentAdapter.getComponentKey() == expectedNameBinding) {
					result.add(container.getComponentInto(componentAdapter.getComponentKey(), into));
				}
			}
			return result;
		} catch (final InstantiationException e) {
			// /CLOVER:OFF
			throw new PicoCompositionException(e);
			// /CLOVER:ON
		} catch (final IllegalAccessException e) {
			// /CLOVER:OFF
			throw new PicoCompositionException(e);
			// /CLOVER:ON
		}
	}

	@SuppressWarnings({ "unchecked" })
	private Map getMapInstance(final PicoContainer container, final Class<? extends Map> expectedType,
			final Map<Object, ComponentAdapter<?>> adapterList, final Type into) {
		Class<? extends Map> collectionType = expectedType;
		if (collectionType.isInterface()) {
			// The order of tests are significant. The least generic types last.
			if (SortedMap.class.isAssignableFrom(collectionType)) {
				collectionType = TreeMap.class;
				// } else if
				// (ConcurrentMap.class.isAssignableFrom(collectionType)) {
				// collectionType = ConcurrentHashMap.class;
			} else if (Map.class.isAssignableFrom(collectionType)) {
				collectionType = HashMap.class;
			}
		}
		try {
			final Map result = collectionType.newInstance();
			for (final Map.Entry<Object, ComponentAdapter<?>> entry : adapterList.entrySet()) {
				final Object key = entry.getKey();
				result.put(key, container.getComponentInto(key, into));
			}
			return result;
		} catch (final InstantiationException e) {
			// /CLOVER:OFF
			throw new PicoCompositionException(e);
			// /CLOVER:ON
		} catch (final IllegalAccessException e) {
			// /CLOVER:OFF
			throw new PicoCompositionException(e);
			// /CLOVER:ON
		}
	}
}
