/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy;

import java.util.Collection;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;

/**
 * This class can generate a Groovy script from a preconfigured container.
 * This script can be passed to {@link GroovyContainerBuilder} to recreate
 * a new container with the same configuration.
 * <p/>
 * This is practical in situations where a container configuration needs
 * to be saved.
 *
 * @author Aslak Helles&oslash;y
 */
public class GroovyScriptGenerator {
    // This implementation is ugly and naive. But it's all I need for now.
    // When there are more requirements (in the form of tests), we can improve this.
    public String generateScript(final MutablePicoContainer pico) {
        StringBuffer groovy = new StringBuffer();
        groovy.append("pico = new org.picocontainer.classname.DefaultClassLoadingPicoContainer()\n");

        Collection<ComponentAdapter<?>> componentAdapters = pico.getComponentAdapters();
        for (ComponentAdapter<?> componentAdapter : componentAdapters) {
            Object key = componentAdapter.getComponentKey();
            String groovyKey = null;
            if (key instanceof Class) {
                groovyKey = ((Class<?>) key).getName();
            } else if (key instanceof String) {
                groovyKey = "\"" + key + "\"";
            }

            Object componentInstance = componentAdapter.getComponentInstance(pico, null);

            if (componentInstance instanceof String) {
                groovy.append("pico.addComponent(")
                    .append(groovyKey)
                    .append(", (Object) \"")
                    .append(componentInstance)
                    .append("\")\n");
            } else {
                groovy.append("pico.addComponent(")
                    .append(groovyKey)
                    .append(", ")
                    .append(componentInstance.getClass().getName())
                    .append(")\n");
            }
        }
        return groovy.toString();
    }
}