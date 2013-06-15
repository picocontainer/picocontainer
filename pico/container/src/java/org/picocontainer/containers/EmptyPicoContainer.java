/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/
package org.picocontainer.containers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.converters.ConvertsNothing;

import com.googlecode.jtype.Generic;

/**
 * Empty pico container serving as recoil damper in situations where you
 * do not like to check whether container reference supplied to you
 * is null or not
 *
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public class EmptyPicoContainer implements PicoContainer, Converting, Serializable {

    public Object getComponent(final Object keyOrType) {
        return null;
    }

    public Object getComponentInto(final Object keyOrType, final Type into) {
        return null;
    }

    public <T> T getComponent(final Class<T> componentType) {
        return null;
    }

    public <T> T getComponent(final Generic<T> componentType) {
        return null;
    }

    public <T> T getComponentInto(final Class<T> componentType, final Type into) {
        return null;
    }

    public <T> T getComponentInto(final Generic<T> componentType, final Type into) {
        return null;
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding, final Type into) {
        return null;
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return null;
    }

    public List<Object> getComponents() {
        return Collections.emptyList();
    }

    public PicoContainer getParent() {
        return null;
    }

    public ComponentAdapter<?> getComponentAdapter(final Object key) {
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding nameBinding) {
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final NameBinding nameBinding) {
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        return null;
    }

    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        return Collections.emptyList();
    }

    /**
     * we do not have anything to do here.
     */
    public void accept(final PicoVisitor visitor) {
        //Does nothing.
    }

    /** {@inheritDoc} **/
    public <T> List<T> getComponents(final Class<T> componentType) {
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
