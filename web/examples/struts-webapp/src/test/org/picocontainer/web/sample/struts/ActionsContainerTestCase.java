package org.picocontainer.web.sample.struts;

import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.picocontainer.PicoContainer;
import org.picocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.script.xml.XStreamContainerBuilder;

/**
 * @author Mauro Talevi
 */
public final class ActionsContainerTestCase {

    protected PicoContainer buildContainer(Reader script) {
        ScriptedContainerBuilder builder = new XStreamContainerBuilder(script, getClass().getClassLoader());
        return builder.buildContainer(null, "SOME_SCOPE", true);
    }

    @Test
    public void testContainerBuildingWithXmlConfig() {

        Reader script = new StringReader("<container>"
                + "	 <implementation type='org.picocontainer.web.sample.struts.CheeseDao'"
                + "					class='org.picocontainer.web.sample.struts.InMemoryCheeseDao'> " + "  </implementation>"
                + "	 <implementation type='org.picocontainer.web.sample.struts.CheeseService'"
                + " 				class='org.picocontainer.web.sample.struts.DefaultCheeseService'>"
                + "  </implementation>" + " </container>");

        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponent(CheeseDao.class));
        assertNotNull(pico.getComponent(CheeseService.class));
    }

}
