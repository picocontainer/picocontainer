package org.picocontainer.web.sample.ajaxemail.scenarios;

import org.picocontainer.web.sample.ajaxemail.scenarios.steps.AjaxEmailScenario;

public class UserReadsAnEmail extends AjaxEmailScenario {

    public UserReadsAnEmail() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public UserReadsAnEmail(final ClassLoader classLoader) {
    	super(classLoader);
    }

}