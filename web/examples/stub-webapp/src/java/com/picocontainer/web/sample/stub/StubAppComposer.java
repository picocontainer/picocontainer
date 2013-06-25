package com.picocontainer.web.sample.stub;

import com.picocontainer.web.WebappComposer;

import com.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

public class StubAppComposer implements WebappComposer {

    public void composeApplication(MutablePicoContainer container, ServletContext context) {
        container.addComponent(AppScoped.class);
    }

    public void composeSession(MutablePicoContainer container) {
        container.addComponent(SessionScoped.class);
    }

    public void composeRequest(MutablePicoContainer container) {
        container.addComponent(RequestScoped.class);
    }
}
