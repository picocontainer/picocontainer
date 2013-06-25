package com.picocontainer.booter;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;

import java.util.Map;

public class BrownBearHelper {

    public BrownBearHelper() {
       ClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        pico.addComponent(Map.class, new ClassName("java.util.HashMap"));
    }

}
