package org.picocontainer.web.sample.ajaxemail.scenarios.pages;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.condition.Presence;
import com.thoughtworks.selenium.condition.ConditionRunner;

public class LoginForm extends Page {

	public LoginForm(Selenium selenium, ConditionRunner runner) {
		super(selenium, runner);
	}

	public Main login(String userName, String password) {
		waitFor(new Presence("id=userName"));
		selenium.type("id=userName", userName);
		selenium.type("id=password", password);
		selenium.click("id=submitLogin");
		return new Main(selenium, runner);
	}

}
