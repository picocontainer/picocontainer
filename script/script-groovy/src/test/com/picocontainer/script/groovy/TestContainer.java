package com.picocontainer.script.groovy;

import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class TestContainer extends DefaultClassLoadingPicoContainer {

    public TestContainer(final ComponentFactory componentFactory, final PicoContainer parent) {
        super(TestContainer.class.getClassLoader(), componentFactory, parent);
    }

    public TestContainer(final PicoContainer parent) {
        super(TestContainer.class.getClassLoader(), new DefaultPicoContainer(parent, new Caching()));
    }

    public TestContainer() {
    }
}
