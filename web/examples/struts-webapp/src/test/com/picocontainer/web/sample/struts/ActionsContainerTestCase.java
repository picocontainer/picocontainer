package com.picocontainer.web.sample.struts;

import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.xml.XStreamContainerBuilder;

import com.picocontainer.PicoContainer;

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
                + "	 <implementation type='com.picocontainer.web.sample.struts.CheeseDao'"
                + "					class='com.picocontainer.web.sample.struts.InMemoryCheeseDao'> " + "  </implementation>"
                + "	 <implementation type='com.picocontainer.web.sample.struts.CheeseService'"
                + " 				class='com.picocontainer.web.sample.struts.DefaultCheeseService'>"
                + "  </implementation>" + " </container>");

        PicoContainer pico = buildContainer(script);
        assertNotNull(pico.getComponent(CheeseDao.class));
        assertNotNull(pico.getComponent(CheeseService.class));
    }

}
