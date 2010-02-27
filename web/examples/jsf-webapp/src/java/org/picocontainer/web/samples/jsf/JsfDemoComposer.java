package org.picocontainer.web.samples.jsf;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Characteristics;
import org.picocontainer.web.WebappComposer;

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
        container.addComponent("cheeseBean", org.picocontainer.web.samples.jsf.ListCheeseController.class);
        container.addComponent("addCheeseBean", org.picocontainer.web.samples.jsf.AddCheeseController.class);
    }

}
