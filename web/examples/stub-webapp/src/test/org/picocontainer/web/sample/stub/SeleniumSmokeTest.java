package org.picocontainer.web.sample.stub;

import com.thoughtworks.selenium.DefaultSelenium;
import static com.thoughtworks.selenium.SeleneseTestCase.assertEquals;
import org.junit.Test;import static junit.framework.Assert.assertTrue;

public class SeleniumSmokeTest {

    protected DefaultSelenium createSeleniumClient(String url) throws Exception {
        return new DefaultSelenium("localhost", 4444, "*firefox", url);
    }

    //@Test
    // This test only works with a Selenium-server build from the end of Jun '08 onwards.
    // The maven plugin as of the end of June, does not have the right server launched for
    // Firefox 3.0
    public void testSomethingSimple() throws Exception {
        DefaultSelenium selenium = createSeleniumClient("http://localhost:8080/");
        selenium.start();

        //selenium.showContextualBanner();
        selenium.open("/stub-webapp");
        selenium.isTextPresent("Dolcelatte");
        selenium.type("name", "test-cheese");
        selenium.type("country", "Irelande");
        selenium.click("//input[@value='Cheese Me!']");
        selenium.isTextPresent("test-cheese");
        selenium.stop();
    }

    @Test
    public void testNothingToFoolTheJUnitRunner() {
        assertTrue(true);
    }



}
