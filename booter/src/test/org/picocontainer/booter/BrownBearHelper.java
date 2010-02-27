package org.picocontainer.booter;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.classname.ClassName;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;

import java.util.Map;

public class BrownBearHelper {

    public BrownBearHelper() {
       ClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        pico.addComponent(Map.class, new ClassName("java.util.HashMap"));
    }

}
