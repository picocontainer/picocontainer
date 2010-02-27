/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.jsf;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.picocontainer.PicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.PicoServletContainerFilter;

/**
 * This is a variable resolver implementation for Java ServerFaces.
 * <h2>Installation</h2>
 * <p>
 * Add install this variable resolver by adding setting the application's
 * variable resolver to
 * <em>org.picocontainer.web.jsf.NanoWarDelegatingVariableResolver</em>. An
 * example follows:
 * </p>
 * <hr/>
 * 
 * <pre>
 *   &lt;faces-config&gt;
 *      &lt;application&gt;
 *          &lt;strong&gt;
 *          &lt;variable-resolver&gt;
 *              org.picocontainer.web.jsf.NanoWarDelegatingVariableResolver
 *          &lt;/variable-resolver&gt;
 *          &lt;/strong&gt;
 *      &lt;/application&gt;
 *      ....
 *   &lt;/faces-config&gt;
 * </pre>
 * 
 * <hr/>
 * <h2>Usage</h2>
 * <h4>Part 1 - Write your Constructor Dependency Injection (CDI) - based
 * backing bean:</h4>
 * <p>
 * Even though you are writing a backing bean, you can utilize PicoContainers
 * CDI capabilities to the fullest. Example:
 * </p>
 * 
 * <pre>
 *    //Imports and variables...
 *    
 *    public ListCheeseController(&lt;strong&gt;CheeseService service&lt;/strong&gt;) {
 *       this.service = service;       
 *    }
 *    
 *    //The rest of the class.
 * </pre>
 * 
 * <h4>Part 2 - Set up your NanoWAR services.</h4>
 * <p>
 * (This assumes you have installed NanoWAR properly. Please see the NanoWAR
 * documentation for specific instructions)
 * </p>
 * <p>
 * You need to name your services with the name you will be giving your
 * <tt>Backing Bean</tt>. Example:
 * 
 * <pre>
 *    pico = builder.container(parent: parent) {
 *        if(assemblyScope instanceof javax.servlet.ServletContext) {
 *          // Application Services would go here.
 *        } else if (assemblyScope instanceof javax.servlet.ServletRequest) {
 *            &lt;strong&gt;addComponent(key: 'cheeseBean', class: 'org.picocontainer.web.samples.jsf.ListCheeseController')&lt;/strong&gt;
 *        }
 *    }
 * </pre>
 * 
 * <h4>Part 3 - Set up your managed beans for JSF</h4>
 * <p>
 * Set the managed bean names in your <tt>faces-config</tt> to equal the names
 * given to the backing beans in the nanowar composition script. Example:
 * </p>
 * 
 * <pre>
 *    &lt;managed-bean&gt;
 *        &lt;description&gt;CDI Injected Bean&lt;/description&gt;
 *        &lt;strong&gt;&lt;managed-bean-name&gt;cheeseBean&lt;/managed-bean-name&gt;&lt;/strong&gt;
 *        &lt;managed-bean-class&gt;
 *            org.picocontainer.web.samples.jsf.CheeseController
 *        &lt;/managed-bean-class&gt;
 *        &lt;managed-bean-scope&gt;request&lt;/managed-bean-scope&gt;
 *    &lt;/managed-bean&gt;
 * </pre>
 * 
 * <p>
 * Notice how the same names were used in the <tt>faces-config</tt> as in the
 * nanowar configuration. When the JSF page asks for the bean named
 * 'addCheeseBean', the Nano variable resolver will take that name and check
 * nanocontainer for an object of that instance. If it finds one, it will send
 * it back to the page.
 * </p>
 * <em>Note:</em>
 * <p>
 * This class currently has only been tested using MyFaces. There are reports
 * that this technique doesn't work on all reference implementation versions. We
 * welcome success or failure feedback!
 * </p>
 * 
 * @author Michael Rimov
 */
public class PicoVariableResolver extends VariableResolver {

    public static class ServletFilter extends PicoServletContainerFilter {
        private static ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        private static ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
        private static ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();

        protected void setAppContainer(MutablePicoContainer container) {
            currentAppContainer.set(container);
        }
        protected void setRequestContainer(MutablePicoContainer container) {
            currentRequestContainer.set(container);
        }
        protected void setSessionContainer(MutablePicoContainer container) {
            currentSessionContainer.set(container);
        }

        protected static MutablePicoContainer getRequestContainerForThread() {
            return currentRequestContainer.get();
        }
        protected static MutablePicoContainer getSessionContainerForThread() {
            return currentSessionContainer.get();
        }
        protected static MutablePicoContainer getApplicationContainerForThread() {
            return currentAppContainer.get();
        }

    }
    /**
     * The nested variable resolver.
     */
    private VariableResolver nested;

    /**
     * Decorated Variable resolver.
     * 
     * @param decorated
     */
    public PicoVariableResolver(VariableResolver decorated) {
        super();
        if (decorated == null) {
            throw new NullPointerException("decorated");
        }
        nested = decorated;
    }

    /**
     * Retrieve the delegated value.
     * 
     * @return the wrapped variable resolver.
     */
    protected VariableResolver getNested() {
        return nested;
    }

    /**
     * {@inheritDoc}
     * 
     * @param facesContext
     * @param name
     * @return the resulting object, either resolved through NanoWAR, or passed
     *         onto the delegate resolver.
     * @throws EvaluationException
     * @see javax.faces.el.VariableResolver#resolveVariable(javax.faces.context.FacesContext,
     *      java.lang.String)
     */
    public Object resolveVariable(FacesContext facesContext, String name) {

        PicoContainer nano = getPicoContainer(facesContext);

        Object result = nano.getComponent(name);
        if (result == null) {
            return nested.resolveVariable(facesContext, name);
        }

        return result;
    }

    /**
     * Tries to locate the nanocontainer first at request level, and then if it
     * doesn't find it there. (Filter might not be installed), it tries
     * Application level. If that fails it throws an exception since you
     * wouldn't expect the NanoWarDelegatingVariableResolver
     * 
     * @param facesContext
     * @return NanoContainer instance.
     * @throws EvaluationException if it cannot find a NanoWAR instance.
     */
    protected PicoContainer getPicoContainer(FacesContext facesContext) {
        Map requestAttributeMap = facesContext.getExternalContext().getRequestMap();

        PicoContainer container = null;

        // First check request map.
        if (requestAttributeMap != null) {
            container = ServletFilter.getRequestContainerForThread();
        }

        if (requestAttributeMap == null || container == null) {

            // If that fails, check session for container.
            Map sessionMap = facesContext.getExternalContext().getSessionMap();
            if (sessionMap != null) {
                // If there is a session.
                container = ServletFilter.getSessionContainerForThread();
            }

            if (sessionMap == null || container == null) {

                // If that fails, check for App level container.
                container = ServletFilter.getApplicationContainerForThread();
                if (container == null) {
                    // If that fails... Fail.
                    throw new EvaluationException(
                            "The NanoWar delegating variable resolver is installed, however no NanoWar "
                                    + "container was found in the request or application scope.");
                }
            }
        }

        return container;
    }

}
