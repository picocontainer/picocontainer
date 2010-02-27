package org.picocontainer.script.groovy;

import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class TestContainer extends DefaultClassLoadingPicoContainer {

    public TestContainer(ComponentFactory componentFactory, PicoContainer parent) {
        super(TestContainer.class.getClassLoader(), componentFactory, parent);
    }

    public TestContainer(PicoContainer parent) {
        super(TestContainer.class.getClassLoader(), new DefaultPicoContainer(new Caching(), parent));
    }

    public TestContainer() {
    }
}
