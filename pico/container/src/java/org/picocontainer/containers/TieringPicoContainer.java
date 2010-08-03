package org.picocontainer.containers;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.TypeOf;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.injectors.AdaptingInjection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@SuppressWarnings("serial")
public class TieringPicoContainer extends DefaultPicoContainer {

    /**
     * Creates a new container with a custom ComponentFactory, LifecycleStrategy for instance registration,
     * and a parent container.
     * <em>
     * Important note about caching: If you intend the components to be cached, you should pass
     * in a factory that creates {@link org.picocontainer.behaviors.Caching.Cached} instances, such as for example
     * {@link org.picocontainer.behaviors.Caching}. Caching can delegate to other ComponentAdapterFactories.
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

    public PicoContainer getParent() {
        return new TieringGuard(super.getParent());
    }

    public MutablePicoContainer makeChildContainer() {
        return new TieringPicoContainer(super.componentFactory, super.lifecycle, this, super.monitor);
    }

    private static class TieringGuard extends AbstractDelegatingPicoContainer {

        private static final AskingParentForComponent askingParentForComponent = new AskingParentForComponent();

        public TieringGuard(PicoContainer parent) {
            super(parent);
        }


        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding nameBinding) {
            return getComponentAdapter(TypeOf.fromClass(componentType), nameBinding);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(TypeOf<T> componentType, NameBinding nameBinding) {
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

        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
            return getComponentAdapter(TypeOf.fromClass(componentType), binding);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(TypeOf<T> componentType, Class<? extends Annotation> binding) {
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

        public ComponentAdapter<?> getComponentAdapter(Object key) {
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
        protected Object initialValue() {
            return Boolean.FALSE;
        }
    }
}