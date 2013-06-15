/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.behaviors;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class Intercepting extends AbstractBehavior {

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor,
                                                          final LifecycleStrategy lifecycle,
                                                          final Properties componentProps,
                                                          final Object key,
                                                          final Class<T> impl,
                                                          final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        return monitor.changedBehavior(new Intercepted<T>(super.createComponentAdapter(monitor,
                lifecycle, componentProps, key,
                impl, constructorParams, fieldParams, methodParams)));
    }

    /**
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static class Intercepted<T> extends ImplementationHiding.HiddenImplementation<T> {

        private final Map<Class, Object> pres = new HashMap<Class, Object>();
        private final Map<Class, Object> posts = new HashMap<Class, Object>();
        private final Controller controller = new ControllerWrapper(new InterceptorThreadLocal());

        public Intercepted(final ComponentAdapter<T> delegate) {
            super(delegate);
        }

        public void addPreInvocation(final Class type, final Object interceptor) {
            pres.put(type, interceptor);
        }

        public void addPostInvocation(final Class type, final Object interceptor) {
            posts.put(type, interceptor);
        }

        @Override
        protected Object invokeMethod(final Object componentInstance, final Method method, final Object[] args, final PicoContainer container) throws Throwable {
            try {
                controller.clear();
                controller.instance(componentInstance);
                Object pre = pres.get(method.getDeclaringClass());
                if (pre != null) {
                    Object rv = method.invoke(pre, args);
                    if (controller.isVetoed()) {
                        return rv;
                    }
                }
                Object result = method.invoke(componentInstance, args);
                controller.setOriginalRetVal(result);
                Object post = posts.get(method.getDeclaringClass());
                if (post != null) {
                    Object rv = method.invoke(post, args);
                    if (controller.isOverridden()) {
                        return rv;
                    }
                }
                return result;
            } catch (final InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        }

        public Controller getController() {
            return controller;
        }

        @Override
		public String getDescriptor() {
            return "Intercepted";
        }
    }

    public static class InterceptorThreadLocal extends ThreadLocal<Controller> implements Serializable {

        @Override
		protected Controller initialValue() {
            return new ControllerImpl();
        }
    }

    public interface Controller {
        void veto();

        void clear();

        boolean isVetoed();

        void setOriginalRetVal(Object retVal);

        boolean isOverridden();

        void instance(Object instance);

        Object getInstance();

        Object getOriginalRetVal();

        void override();
    }

    public static class ControllerImpl implements Controller {
        private boolean vetoed;
        private Object retVal;
        private boolean overridden;
        private Object instance;

        public void veto() {
            vetoed = true;
        }

        public void clear() {
            vetoed = false;
            overridden = false;
            retVal = null;
            instance = null;
        }

        public boolean isVetoed() {
            return vetoed;
        }

        public void setOriginalRetVal(final Object retVal) {
            this.retVal = retVal;
        }

        public Object getOriginalRetVal() {
            return retVal;
        }

        public boolean isOverridden() {
            return overridden;
        }

        public void instance(final Object instance) {
            this.instance = instance;
        }

        public Object getInstance() {
            return instance;
        }

        public void override() {
            overridden = true;
        }
    }

    public static class ControllerWrapper implements Controller {
        private final ThreadLocal<Controller> threadLocal;

        public ControllerWrapper(final ThreadLocal<Controller> threadLocal) {
            this.threadLocal = threadLocal;
        }

        public void veto() {
            threadLocal.get().veto();
        }

        public void clear() {
            threadLocal.get().clear();
        }

        public boolean isVetoed() {
            return threadLocal.get().isVetoed();
        }

        public void setOriginalRetVal(final Object retVal) {
            threadLocal.get().setOriginalRetVal(retVal);
        }

        public Object getOriginalRetVal() {
            return threadLocal.get().getOriginalRetVal();
        }

        public boolean isOverridden() {
            return threadLocal.get().isOverridden();
        }

        public void instance(final Object instance) {
            threadLocal.get().instance(instance);

        }

        public Object getInstance() {
            return threadLocal.get().getInstance();
        }

        public void override() {
            threadLocal.get().override();
        }
    }
}
