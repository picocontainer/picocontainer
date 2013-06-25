package com.picocontainer.web.samples.jsf;

import com.picocontainer.web.WebappComposer;

import com.picocontainer.Characteristics;
import com.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

public class JsfDemoComposer implements WebappComposer {

    public void composeApplication(MutablePicoContainer container, ServletContext context) {
        container.addComponent(CheeseDao.class, InMemoryCheeseDao.class);
    }

    public void composeSession(MutablePicoContainer container) {
        container.addComponent(CheeseService.class, DefaultCheeseService.class);
    }

    public void composeRequest(MutablePicoContainer container) {
        container.as(Characteristics.NO_CACHE).addComponent(Brand.class, Brand.FromRequest.class);
        container.addComponent("cheeseBean", com.picocontainer.web.samples.jsf.ListCheeseController.class);
        container.addComponent("addCheeseBean", com.picocontainer.web.samples.jsf.AddCheeseController.class);
    }

}
