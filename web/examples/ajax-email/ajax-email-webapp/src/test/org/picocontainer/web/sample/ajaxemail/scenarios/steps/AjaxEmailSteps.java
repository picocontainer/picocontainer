package org.picocontainer.web.sample.ajaxemail.scenarios.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jbehave.scenario.annotations.Aliases;
import org.jbehave.scenario.annotations.BeforeScenario;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.web.selenium.SeleniumContext;
import org.jbehave.web.selenium.SeleniumSteps;
import org.jbehave.web.selenium.SeleniumStepsConfiguration;
import org.picocontainer.web.sample.ajaxemail.scenarios.pages.LoginForm;
import org.picocontainer.web.sample.ajaxemail.scenarios.pages.Main;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.condition.Condition;
import com.thoughtworks.selenium.condition.Not;
import com.thoughtworks.selenium.condition.Text;

public class AjaxEmailSteps extends SeleniumSteps {

    private Main main;
    private String[] lastFormValues;
    private String prefix;

    public AjaxEmailSteps(Selenium selenium, SeleniumContext seleniumContext) {
        super(new SeleniumStepsConfiguration(selenium, seleniumContext));
    }

    @Override
    @BeforeScenario    
    public void beforeScenario() throws Exception {
        super.beforeScenario();
        selenium.setSpeed("750");
        selenium.getEval( "window.moveTo(1,250); window.resizeTo(800,537);" );
    }

    @Given("test data")
    @Aliases(values="sample data is loaded")
	public void sampleData() {
        selenium.open("/ajaxemail-webapp/json/SampleData/load");
    }

    @Given("nobody is logged in")
	public void nobodyLoggedIn() {
        Main.logout(selenium);
    }

    @Given("user is viewing their Inbox")
	public void theyreInTheirInbox() {
        nobodyLoggedIn();
        logIn("Gill Bates", "1234");
        boxIsSelected("Inbox");
    }

    @When("user $userName with password $password attempts to log in")
	public void logIn(String userName, String password) {
        main = new LoginForm(selenium, runner).login(userName, password);
	}

    @When("the mail-form is filled")
	public void mailFormFilled() {
        lastFormValues = main.formFieldValues("compose", true, "to", "subject");
	}

    @When("the first email listed is double-clicked to $prefix an email")
	public void firstEmailDoubleClicked(String prefix) {
        this.prefix = prefix;
        main.firstEmailDoubleClicked();
        lastFormValues = main.formFieldValues(prefix, false);
	}

    @Then("the $box is selected")
    @Aliases(values="the $box is displayed")
	public void boxIsSelected(String box) {
		main.selectedBox(box);
	}

    @When("the '$button' button is clicked")
	public void clickButton(String legend) {
		main.clickButton(legend);
	}

    @Then("the main page should $beOrNotBe obscured")
	public void mainPageObscured(String beOrNotBe) {
        if ("be".equals(beOrNotBe)) {
		    main.mainPageObscured();
        } else if ("not be".equals(beOrNotBe)) {
		    main.mainPageNotObscured();
        } else {
            fail("'be' or 'not be' are your choices, not: '" + beOrNotBe +"'");
        }
	}

    @Then("a blocking mail-form should be $visibleOrGone")
	public void blockingMailFormPresent(String visibleOrGone) {
        if ("visible".equals(visibleOrGone)) {
		    main.blockingMailFormPresent();
        } else if ("gone".equals(visibleOrGone)) {
		    main.blockingMailFormNotPresent();
        } else {
            fail("'visible' or 'gone' are your choices, not: '" + visibleOrGone +"'");
        }
	}

    @Then("$fields in the form should be blank")
	public void withNothingInIt(String fields) {
        main.fieldsAreBlank(fields.split(","));
	}

    @Then("the mail-form should show the clicked email")
	public void mailFormShouldShowClickedEmail() {
        waitFor(1);
		String[] values = main.formFieldValues(prefix, false, "from", "subject");
        System.err.println("");
        for (int i = 0; i < lastFormValues.length; i++) {
            assertEquals(lastFormValues[i], values[i]);
        }
	}

	@Then("the Inbox should not be visible")
	public void inBoxIsNotVisible() {
		main.textIsNotVisible("Instant Millionaire");
	}

	@Then("the text \"$text\" should be visible")
	public void textIsVisible(String text) {
		main.waitFor(new Text(text));
	}

	@Then("there are $qty messages listed")
	public void numMessages(String qty) {
        qty = qty.replace("no","0");
        int ct = main.numberOfMailItemsVisible();        
        if ("some".equals(qty)) {
            assertTrue(ct > 0);
        } else {
            assertEquals(qty, ""+ct);
        }
	}

	@Then("the text \"$text\" should not be visible")
	public void textIsNotVisible(String text) {
		waitFor(new Not(new Text(text)));
	}

    @Then("the mail should be visible in the sent mails list")
    public void mailIsVisibleInSentMails() {
        main.clickLink("Sent");
        assertTrue(lastFormValues.length > 0);
        for (String lastFormValue : lastFormValues) {
            main.waitFor(new Text(lastFormValue));
        }
    }

	private void waitFor(Condition condition) {
		runner.waitFor(condition);
		waitFor(1);
	}

}
