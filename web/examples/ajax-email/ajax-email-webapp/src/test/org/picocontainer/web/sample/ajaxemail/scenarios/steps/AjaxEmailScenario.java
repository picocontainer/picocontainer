package org.picocontainer.web.sample.ajaxemail.scenarios.steps;

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.PropertyBasedConfiguration;
import org.jbehave.scenario.errors.PendingErrorStrategy;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.web.selenium.SeleniumContext;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class AjaxEmailScenario extends JUnitScenario {

    private Selenium selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://localhost:8080");
	private AjaxEmailSteps ajaxEmailSteps;

    public AjaxEmailScenario() {
        this(Thread.currentThread().getContextClassLoader(), new SeleniumContext());
    }

    public AjaxEmailScenario(final ClassLoader classLoader) {
        this(classLoader, new SeleniumContext());
    }

    public AjaxEmailScenario(final ClassLoader classLoader, final SeleniumContext seleniumContext) {
        super(new PropertyBasedConfiguration() {
            @Override
            public ClasspathScenarioDefiner forDefiningScenarios() {
                return new ClasspathScenarioDefiner(new UnderscoredCamelCaseResolver(".scenario"),
                        new PatternScenarioParser(this), classLoader);
            }
            @Override
            public PendingErrorStrategy forPendingSteps() {
                return PendingErrorStrategy.FAILING;
            }
            @Override
			public ScenarioReporter forReportingScenarios() {
				return new PrintStreamScenarioReporter() {
                    @Override
                    public void beforeScenario(String title) {
                        seleniumContext.setCurrentScenario(title);
                        super.beforeScenario(title);
                    }
                };
			}
        });

        ajaxEmailSteps = new AjaxEmailSteps(selenium, seleniumContext);
		super.addSteps(ajaxEmailSteps);
    }
    
    public AjaxEmailSteps getAjaxEmailSteps() {
		return ajaxEmailSteps;
	}

}