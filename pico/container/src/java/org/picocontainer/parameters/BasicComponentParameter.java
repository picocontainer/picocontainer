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

import com.googlecode.jtype.Generic;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.JTypeHelper;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.InjectInto;

import javax.inject.Provider;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A BasicComponentParameter should be used to pass in a particular component as argument to a
 * different component's constructor. This is particularly useful in cases where several
 * components of the same type have been registered, but with a different key. Passing a
 * ComponentParameter as a parameter when registering a component will give PicoContainer a hint
 * about what other component to use in the constructor. This Parameter will never resolve
 * against a collecting type, that is not directly registered in the PicoContainer itself.
 *
 * @author Jon Tirs&eacute;n
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 * @author Thomas Heller
 */
@SuppressWarnings("serial")
public class BasicComponentParameter extends AbstractParameter implements Parameter, Serializable {

    /** <code>BASIC_DEFAULT</code> is an instance of BasicComponentParameter using the default constructor. */
    public static final BasicComponentParameter BASIC_DEFAULT = new BasicComponentParameter();

    private Object key;

    /**
     * Expect a parameter matching a component of a specific key.
     *
     * @param key the key of the desired addComponent
     */
    public BasicComponentParameter(Object key) {
        this.key = key;
    }

    /** Expect any parameter of the appropriate type. */
    public BasicComponentParameter() {
    }

    /**
     * Check whether the given Parameter can be satisfied by the container.
     *
     * @return <code>true</code> if the Parameter can be verified.
     *
     * @throws org.picocontainer.PicoCompositionException
     *          {@inheritDoc}
     * @see Parameter#isResolvable(PicoContainer, ComponentAdapter, Class, NameBinding ,boolean, Annotation)
     */
    public Resolver resolve(final PicoContainer container,
                            final ComponentAdapter<?> forAdapter,
                            final ComponentAdapter<?> injecteeAdapter, final Type expectedType,
                            NameBinding expectedNameBinding, boolean useNames, Annotation binding) {
    	
    	Generic<?> resolvedClassType = null;
        // TODO take this out for Pico3
        if (notAClass(expectedType) && notAJsr330Provider(expectedType)) {
        	if (expectedType instanceof ParameterizedType) {
        		resolvedClassType = Generic.get((ParameterizedType)expectedType);
        	} else {
        		return new Parameter.NotResolved();
        	}
        } else if (expectedType instanceof ParameterizedType) {
            resolvedClassType = Generic.get((ParameterizedType) expectedType);
        } else {
        	resolvedClassType = Generic.get((Class<?>) expectedType);
        }
        assert resolvedClassType != null;

        ComponentAdapter<?> componentAdapter0;
        if (injecteeAdapter == null) {
            componentAdapter0 = resolveAdapter(container, forAdapter, resolvedClassType, expectedNameBinding, useNames, binding);
        } else {
            componentAdapter0 = injecteeAdapter;
        }
        final ComponentAdapter<?> componentAdapter = componentAdapter0;
        return new Resolver() {
            public boolean isResolved() {
                return componentAdapter != null;
            }
            public Object resolveInstance(Type into) {
                if (componentAdapter == null) {
                    return null;
                }
                if (componentAdapter instanceof DefaultPicoContainer.LateInstance) {
                    return convert(getConverters(container), ((DefaultPicoContainer.LateInstance) componentAdapter).getComponentInstance(), expectedType);
//                } else if (injecteeAdapter != null && injecteeAdapter instanceof DefaultPicoContainer.KnowsContainerAdapter) {
//                    return convert(((DefaultPicoContainer.KnowsContainerAdapter) injecteeAdapter).getComponentInstance(makeInjectInto(forAdapter)), expectedType);
                } else {
                    return convert(getConverters(container), container.getComponentInto(componentAdapter.getComponentKey(), makeInjectInto(forAdapter)), expectedType);
                }
            }

            public ComponentAdapter<?> getComponentAdapter() {
                return componentAdapter;
            }
        };
    }

    private boolean notAJsr330Provider(Type expectedType) {
        return !(expectedType instanceof ParameterizedType
                && ((ParameterizedType) expectedType).getRawType() == Provider.class);
    }

    private boolean notAClass(Type expectedType) {
        return !(expectedType instanceof Class);
    }

    private Converters getConverters(PicoContainer container) {
        return container instanceof Converting ? ((Converting) container).getConverters() : null;
    }

    private static InjectInto makeInjectInto(ComponentAdapter<?> forAdapter) {
        return new InjectInto(forAdapter.getComponentImplementation(), forAdapter.getComponentKey());
    }

    private static Object convert(Converters converters, Object obj, Type expectedType) {
        if (obj instanceof String && expectedType != String.class) {
            obj = converters.convert((String) obj, expectedType);
        }
        return obj;
    }

    public void verify(PicoContainer container,
                       ComponentAdapter<?> forAdapter,
                       Type expectedType,
                       NameBinding expectedNameBinding, boolean useNames, Annotation binding) {
        final ComponentAdapter<?> componentAdapter =
            resolveAdapter(container, forAdapter, Generic.get((Class<?>) expectedType), expectedNameBinding, useNames, binding);
        if (componentAdapter == null) {
            final Set<Type> set = new HashSet<Type>();
            set.add(expectedType);
            throw new AbstractInjector.UnsatisfiableDependenciesException(
                    forAdapter.getComponentImplementation().getName() 
                    + " has unsatisfied dependencies: " + set + " from " + container);
        }
        componentAdapter.verify(container);
    }

    /**
     * Visit the current {@link Parameter}.
     *
     * @see org.picocontainer.Parameter#accept(org.picocontainer.PicoVisitor)
     */
    public void accept(final PicoVisitor visitor) {
        visitor.visitParameter(this);
    }

    protected <T> ComponentAdapter<T> resolveAdapter(PicoContainer container,
                                                   ComponentAdapter adapter,
                                                   Generic<T> expectedType,
                                                   NameBinding expectedNameBinding, boolean useNames, Annotation binding) {
        Generic type = expectedType;
        if (JTypeHelper.isPrimitive(type)) {
            String expectedTypeName = type.toString();
            if (expectedTypeName == "int") {
                type = JTypeHelper.INTEGER;
            } else if (expectedTypeName == "long") {
                type = JTypeHelper.LONG;
            } else if (expectedTypeName == "float") {
                type = JTypeHelper.FLOAT;
            } else if (expectedTypeName == "double") {
                type = JTypeHelper.DOUBLE;
            } else if (expectedTypeName == "boolean") {
                type = JTypeHelper.BOOLEAN;
            } else if (expectedTypeName == "char") {
                type = JTypeHelper.CHARACTER;
            } else if (expectedTypeName == "short") {
                type = JTypeHelper.SHORT;
            } else if (expectedTypeName == "byte") {
                type = JTypeHelper.BYTE;
            }
        }

        ComponentAdapter<T> result = null;
        if (key != null) {
            // key tells us where to look so we follow
            result = typeComponentAdapter(container.getComponentAdapter(key));
        } else if (adapter == null) {
            result = container.getComponentAdapter(type, NameBinding.NULL);
        } else {
            Object excludeKey = adapter.getComponentKey();
            ComponentAdapter byKey = container.getComponentAdapter((Object)expectedType);
            if (byKey != null && !excludeKey.equals(byKey.getComponentKey())) {
                result = typeComponentAdapter(byKey);
            }

            if (result == null && useNames) {
                ComponentAdapter found = container.getComponentAdapter(expectedNameBinding.getName());
                if ((found != null) && areCompatible(container, expectedType, found) && found != adapter) {
                    result = found;
                }
            }

            if (result == null) {
                List<ComponentAdapter<T>> found = binding == null ? container.getComponentAdapters(expectedType) :
                        container.getComponentAdapters(expectedType, binding.annotationType());
                removeExcludedAdapterIfApplicable(excludeKey, found);
                if (found.size() == 0) {
                    result = noMatchingAdaptersFound(container, expectedType, expectedNameBinding, binding);
                } else if (found.size() == 1) {
                    result = found.get(0);
                } else {
                    throw tooManyMatchingAdaptersFound(expectedType, found);
                }
            }
        }

        if (result == null) {
            return null;
        }

        Class<? extends T> componentImplementation = result.getComponentImplementation();
        if (!JTypeHelper.isAssignableFrom(type, componentImplementation)) {
//            if (!(result.getComponentImplementation() == String.class && stringConverters.containsKey(type))) {
            if (!(result.getComponentImplementation() == String.class && getConverters(container).canConvert(type.getType()))) {
                return null;
            }
        }
        return result;
    }

    @SuppressWarnings({ "unchecked" })
    private static <T> ComponentAdapter<T> typeComponentAdapter(ComponentAdapter<?> componentAdapter) {
        return (ComponentAdapter<T>)componentAdapter;
    }

    private <T> ComponentAdapter<T> noMatchingAdaptersFound(PicoContainer container, Generic<T> expectedType,
                                                            NameBinding expectedNameBinding, Annotation binding) {
        if (container.getParent() != null) {
            if (binding != null) {
                return container.getParent().getComponentAdapter(expectedType, binding.getClass());
            } else {
                return container.getParent().getComponentAdapter(expectedType, expectedNameBinding);
            }
        } else {
            return null;
        }
    }

    private <T> AbstractInjector.AmbiguousComponentResolutionException tooManyMatchingAdaptersFound(Generic<T> expectedType, List<ComponentAdapter<T>> found) {
        Class[] foundClasses = new Class[found.size()];
        for (int i = 0; i < foundClasses.length; i++) {
            foundClasses[i] = found.get(i).getComponentImplementation();
        }
        AbstractInjector.AmbiguousComponentResolutionException exception = new AbstractInjector.AmbiguousComponentResolutionException(expectedType, foundClasses);
        return exception;
    }

    private <T> void removeExcludedAdapterIfApplicable(Object excludeKey, List<ComponentAdapter<T>> found) {
        ComponentAdapter exclude = null;
        for (ComponentAdapter work : found) {
            if (work.getComponentKey().equals(excludeKey)) {
                exclude = work;
                break;
            }
        }
        found.remove(exclude);
    }

    private <T> boolean areCompatible(PicoContainer container, Generic<T> expectedType, ComponentAdapter found) {
        Class foundImpl = found.getComponentImplementation();
        return JTypeHelper.isAssignableFrom(expectedType, foundImpl) ||
               (foundImpl == String.class && getConverters(container) != null
                       && getConverters(container).canConvert(expectedType.getType()))  ;
    }
}
