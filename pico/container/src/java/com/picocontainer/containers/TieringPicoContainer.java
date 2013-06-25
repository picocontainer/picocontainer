package com.picocontainer.containers;

import java.lang.annotation.Annotation;


import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.AdaptingBehavior;
import com.picocontainer.injectors.AdaptingInjection;

@SuppressWarnings("serial")
public class TieringPicoContainer extends DefaultPicoContainer {

    /**
     * Creates a new container with a custom ComponentFactory, LifecycleStrategy for instance registration,
     * and a parent container.
     * <em>
     * Important note about caching: If you intend the components to be cached, you should pass
     * in a factory that creates {@link com.picocontainer.behaviors.Caching.Cached} instances, such as for example
     * {@link com.picocontainer.behaviors.Caching}. Caching can delegate to other ComponentAdapterFactories.
     * </em>
     *
     * @param componentFactory the factory to use for creation of ComponentAdapters.
     * @param lifecycle
     *                                the lifecycle strategy chosen for registered
     *                                instance (not implementations!)
     * @param parent                  the parent container (used for component dependency lookups).
     */
    public TieringPicoContainer(final ComponentFactory componentFactory, final LifecycleStrategy lifecycle,
                                final PicoContainer parent) {
        super(parent, lifecycle, componentFactory);
    }

    public TieringPicoContainer(final ComponentFactory componentFactory, final LifecycleStrategy lifecycle,
                                final PicoContainer parent, final ComponentMonitor monitor) {
        super(parent, lifecycle, monitor, componentFactory);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor
     *
     * @param monitor the ComponentMonitor to use
     * @param parent  the parent container (used for component dependency lookups).
     */
    public TieringPicoContainer(final ComponentMonitor monitor, final PicoContainer parent) {
        super(parent, monitor);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor and lifecycle strategy
     *
     * @param monitor           the ComponentMonitor to use
     * @param lifecycle the lifecycle strategy to use.
     * @param parent            the parent container (used for component dependency lookups).
     */
    public TieringPicoContainer(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                final PicoContainer parent) {
        super(parent, lifecycle, monitor);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom lifecycle strategy
     *
     * @param lifecycle the lifecycle strategy to use.
     * @param parent            the parent container (used for component dependency lookups).
     */
    public TieringPicoContainer(final LifecycleStrategy lifecycle, final PicoContainer parent) {
        super(parent, lifecycle);
    }


    /**
     * Creates a new container with a custom ComponentFactory and no parent container.
     *
     * @param componentFactory the ComponentFactory to use.
     */
    public TieringPicoContainer(final ComponentFactory componentFactory) {
        super(componentFactory);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor
     *
     * @param monitor the ComponentMonitor to use
     */
    public TieringPicoContainer(final ComponentMonitor monitor) {
        super(monitor);
    }

    /**
     * Creates a new container with a (caching) {@link AdaptingInjection}
     * and a parent container.
     *
     * @param parent the parent container (used for component dependency lookups).
     */
    public TieringPicoContainer(final PicoContainer parent) {
        super(parent);
    }

    /** Creates a new container with a {@link AdaptingBehavior} and no parent container. */
    public TieringPicoContainer() {
        super();
    }

    @Override
	public PicoContainer getParent() {
        return new TieringGuard(super.getParent());
    }

    @Override
	public MutablePicoContainer makeChildContainer() {
        return new TieringPicoContainer(super.componentFactory, super.lifecycle, this, super.monitor);
    }

    private static class TieringGuard extends AbstractDelegatingPicoContainer {

        private static final AskingParentForComponent askingParentForComponent = new AskingParentForComponent();

        public TieringGuard(final PicoContainer parent) {
            super(parent);
        }


        @Override
		public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding nameBinding) {
            return getComponentAdapter(Generic.get(componentType), nameBinding);
        }

        @Override
		public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final NameBinding nameBinding) {
            boolean iDidIt = false;
            try {
                if (notYetAskingParentForComponent()) {
                    nowAskingParentForComponent();
                    iDidIt = true;
                    return super.getComponentAdapter(componentType, nameBinding);
                } else {
                    return null;
                }
            } finally {
                if (iDidIt) {
                    doneAskingParentForComponent();
                }
            }
        }

        private <T> void nowAskingParentForComponent() {
            askingParentForComponent.set(Boolean.TRUE);
        }

        @Override
		public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
            return getComponentAdapter(Generic.get(componentType), binding);
        }

        @Override
		public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final Class<? extends Annotation> binding) {
            boolean iDidIt = false;
            try {
                if (notYetAskingParentForComponent()) {
                    nowAskingParentForComponent();
                    iDidIt = true;
                    return super.getComponentAdapter(componentType, binding);
                } else {
                    return null;
                }
            } finally {
                if (iDidIt) {
                    doneAskingParentForComponent();
                }
            }
        }

        private <T> void doneAskingParentForComponent() {
            askingParentForComponent.set(Boolean.FALSE);
        }

        private <T> boolean notYetAskingParentForComponent() {
            return askingParentForComponent.get() == Boolean.FALSE;
        }

        @Override
		public ComponentAdapter<?> getComponentAdapter(final Object key) {
            boolean iDidIt = false;
            try {
                if (notYetAskingParentForComponent()) {
                    nowAskingParentForComponent();
                    iDidIt = true;
                    return super.getComponentAdapter(key);
                } else {
                    return null;
                }
            } finally {
                if (iDidIt) {
                    doneAskingParentForComponent();
                }
            }
        }
    }
    private static class AskingParentForComponent extends ThreadLocal {
        @Override
		protected Object initialValue() {
            return Boolean.FALSE;
        }
    }
}