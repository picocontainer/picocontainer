/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.picocontainer.Behavior;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.InjectionType;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoClassNotFoundException;
import com.picocontainer.PicoContainer;
import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.containers.TransientPicoContainer;

/**
 * Facade to build ScriptedScriptedPicoContainer
 *
 * @author Paul Hammant
 */
public final class ScriptedBuilder {

    private Class<? extends ClassLoadingPicoContainer> scriptClass = DefaultClassLoadingPicoContainer.class;
    private final PicoBuilder picoBuilder;
    private ClassLoader classLoader = DefaultClassLoadingPicoContainer.class.getClassLoader();
    private final List<URL> urls = new ArrayList<URL>();

    public ScriptedBuilder(final PicoContainer parentcontainer, final InjectionType injectionType) {
        picoBuilder = new PicoBuilder(parentcontainer, injectionType);
    }

    public ScriptedBuilder(final PicoContainer parentcontainer) {
        picoBuilder = new PicoBuilder(parentcontainer);
    }

    public ScriptedBuilder(final InjectionType injectionType) {
        picoBuilder = new PicoBuilder(injectionType);
    }

    public ScriptedBuilder() {
        picoBuilder = new PicoBuilder();
    }

    public ClassLoadingPicoContainer build() {
        DefaultPicoContainer tpc = new TransientPicoContainer();
        tpc.addComponent(ClassLoader.class, classLoader);
        tpc.addComponent("sc", scriptClass);
        tpc.addComponent(MutablePicoContainer.class, picoBuilder.build());
        ClassLoadingPicoContainer pico = (ClassLoadingPicoContainer) tpc.getComponent("sc");
        for (URL url : urls) {
            pico.addClassLoaderURL(url);
        }
        return pico;
    }

    public ScriptedBuilder withConsoleMonitor() {
        picoBuilder.withConsoleMonitor();
        return this;
    }

    public ScriptedBuilder withLifecycle() {
        picoBuilder.withLifecycle();
        return this;
    }

    public ScriptedBuilder withReflectionLifecycle() {
        picoBuilder.withReflectionLifecycle();
        return this;
    }

    public ScriptedBuilder withMonitor(final Class<? extends ComponentMonitor> clazz) {
        picoBuilder.withMonitor(clazz);
        return this;
    }

    public ScriptedBuilder withHiddenImplementations() {
        picoBuilder.withHiddenImplementations();
        return this;
    }

    public ScriptedBuilder withComponentFactory(final ComponentFactory componentFactory) {
        picoBuilder.withComponentFactory(componentFactory);
        return this;
    }

    public ScriptedBuilder withBehaviors(final Behavior... factories) {
        picoBuilder.withBehaviors(factories);
        return this;
    }

    public ScriptedBuilder withSetterInjection() {
        picoBuilder.withSetterInjection();
        return this;
    }

    public ScriptedBuilder withAnnotatedMethodInjection() {
        picoBuilder.withAnnotatedMethodInjection();
        return this;
    }

    public ScriptedBuilder withConstructorInjection() {
        picoBuilder.withConstructorInjection();
        return this;
    }

    public ScriptedBuilder withCaching() {
        picoBuilder.withCaching();
        return this;
    }

    public ScriptedBuilder withSynchronizing() {
        picoBuilder.withSynchronizing();
        return this;
    }

    public ScriptedBuilder implementedBy(final Class<? extends ClassLoadingPicoContainer> scriptedContainerClass) {
        scriptClass = scriptedContainerClass;
        return this;
    }

    public ScriptedBuilder implementedBy(final String scriptedContainerClass) {
        scriptClass = loadClass(scriptedContainerClass, ClassLoadingPicoContainer.class);
        return this;
    }

    public ScriptedBuilder picoImplementedBy(final Class<? extends MutablePicoContainer> picoContainerClass) {
        picoBuilder.implementedBy(picoContainerClass);
        return this;
    }

    public ScriptedBuilder withClassLoader(final ClassLoader usingClassloader) {
        this.classLoader = usingClassloader;
        return this;
    }

    public ScriptedBuilder withComponentFactory(final String componentFactoryName) {
        if (componentFactoryName != null && !componentFactoryName.equals("")) {
            picoBuilder.withComponentFactory(loadClass(componentFactoryName, ComponentFactory.class));
        }
        return this;
    }

    private <T> Class<? extends T> loadClass(final String className, final Class<T> asClass) {
        try {
            return classLoader.loadClass(className).asSubclass(asClass);
        } catch (ClassNotFoundException e) {
            throw new PicoClassNotFoundException(className, e);
        }
    }

    public ScriptedBuilder withMonitor(final String monitorName) {
        if (monitorName != null && !monitorName.equals("")) {
            picoBuilder.withMonitor(loadClass(monitorName, ComponentMonitor.class));
        }
        return this;
    }

    public ScriptedBuilder addClassLoaderURL(final URL url) {
        urls.add(url);
        return this;
    }
}
