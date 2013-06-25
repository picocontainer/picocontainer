package com.picocontainer.web.sample.ajaxemail;

import static com.picocontainer.Characteristics.USE_NAMES;

import javax.servlet.ServletContext;

import com.picocontainer.web.WebappComposer;
import com.picocontainer.web.remoting.PicoWebRemotingMonitor;
import com.picocontainer.web.sample.ajaxemail.persistence.InMemoryPersister;
import com.picocontainer.web.sample.ajaxemail.persistence.Persister;

import com.picocontainer.MutablePicoContainer;

public class AjaxEmailWebappComposer implements WebappComposer {

    public void composeApplication(MutablePicoContainer container, ServletContext context) {
        container.addComponent(PicoWebRemotingMonitor.class, AjaxEmailWebRemotingMonitor.class);
        container.addComponent(UserStore.class);
        container.addComponent(Persister.class, InMemoryPersister.class);
        container.addComponent(QueryStore.class);
        container.addComponent(SampleData.class);
//        container.addAdapter(new FallbackCacheProvider());
    }

    public void composeSession(MutablePicoContainer container) {
        // stateless
    }

    public void composeRequest(MutablePicoContainer container) {

        container.addAdapter(new CookieUserProviderAdapter());
        container.as(USE_NAMES).addComponent(Auth.class);
        container.as(USE_NAMES).addComponent(Inbox.class);
        container.as(USE_NAMES).addComponent(Sent.class);

    }

}
