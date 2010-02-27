package org.picocontainer.web.sample.ajaxemail.scenarios;

import org.picocontainer.web.sample.ajaxemail.scenarios.steps.AjaxEmailScenario;


public class UserLogsIn extends AjaxEmailScenario {

    public UserLogsIn() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public UserLogsIn(final ClassLoader classLoader) {
    	super(classLoader);
    }

}