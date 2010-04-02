/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/
package org.picocontainer.containers;

import org.picocontainer.*;
import org.picocontainer.converters.ConvertsNothing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Empty pico container serving as recoil damper in situations where you
 * do not like to check whether container reference supplied to you
 * is null or not
 *
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public class EmptyPicoContainer implements PicoContainer, Converting, Serializable {

    @SuppressWarnings("unused")
    public Object getComponent(Object componentKeyOrType) {
        return null;
    }

    @SuppressWarnings("unused")
    public Object getComponentInto(Object componentKeyOrType, Type into) {
        return null;
    }

    @SuppressWarnings("unused")
    public <T> T getComponent(Class<T> componentType) {
        return null;
    }

    @SuppressWarnings("unused")
    public <T> T getComponentInto(Class<T> componentType, Type into) {
        return null;
    }

    @SuppressWarnings("unused") 
    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding, Type into) {
        return null;
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
        return null;
    }

    public List getComponents() {
        return Collections.EMPTY_LIST;
    }

    public PicoContainer getParent() {
        return null;
    }

    @SuppressWarnings("unused") 
    public ComponentAdapter<?> getComponentAdapter(Object componentKey) {
        return null;
    }

    @SuppressWarnings("unused") 
    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding componentNameBinding) {
        return null;
    }

    @SuppressWarnings("unused") 
    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
        return null;
    }

    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        return Collections.emptyList();
    }

    @SuppressWarnings("unused") 
    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
        return Collections.emptyList();
    }

    @SuppressWarnings("unused") 
    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType, Class<? extends Annotation> binding) {
        return Collections.emptyList();
    }

    /**
     * we do not have anything to do here. 
     */
    @SuppressWarnings("unused") 
    public void accept(PicoVisitor visitor) {
        //Does nothing.
    }

    /** {@inheritDoc} **/
    @SuppressWarnings("unused") 
    public <T> List<T> getComponents(Class<T> componentType) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "(empty)";
    }

    public Converters getConverters() {
        return new ConvertsNothing();
    }

}
