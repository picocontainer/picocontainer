package com.picocontainer.web.sample.struts;

import com.picocontainer.web.WebappComposer;

import com.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

public class Struts1DemoComposer implements WebappComposer {

    public void composeApplication(MutablePicoContainer container, ServletContext context) {
        container.addComponent(CheeseDao.class, InMemoryCheeseDao.class);
    }

    public void composeSession(MutablePicoContainer container) {
        container.addComponent(DefaultCheeseService.class);
    }

    public void composeRequest(MutablePicoContainer container) {
    }
}
