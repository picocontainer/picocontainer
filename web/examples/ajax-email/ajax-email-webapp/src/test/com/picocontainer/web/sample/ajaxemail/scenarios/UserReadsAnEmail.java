package com.picocontainer.web.sample.ajaxemail.scenarios;

import com.picocontainer.web.sample.ajaxemail.scenarios.steps.AjaxEmailScenario;

public class UserReadsAnEmail extends AjaxEmailScenario {

    public UserReadsAnEmail() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public UserReadsAnEmail(final ClassLoader classLoader) {
    	super(classLoader);
    }

}