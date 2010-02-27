package org.picocontainer.web.sample.ajaxemail.scenarios;

import org.picocontainer.web.sample.ajaxemail.scenarios.steps.AjaxEmailScenario;

public class UserSendsNewEmail extends AjaxEmailScenario {

    public UserSendsNewEmail() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public UserSendsNewEmail(final ClassLoader classLoader) {
    	super(classLoader);
    }

}
