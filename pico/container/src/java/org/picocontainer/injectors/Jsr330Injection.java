package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class Jsr330Injection extends ConstructorInjection {

    @Override
    protected <T> ConstructorInjector<T> newConstructorInjector(ComponentMonitor monitor, Object key, Class<T> impl, boolean useNames, Parameter... parameters) {
        return new ConstructorInjector<T>(key, impl, monitor, useNames, rememberChosenConstructor, parameters) {
            @Override
            protected boolean hasApplicableConstructorModifiers(int modifiers) {
                return true;
            }

            @Override
            protected void changeAccessToModifierifNeeded(Constructor<T> ctor) {
                if ((ctor.getModifiers() & Modifier.PUBLIC) == 0) {
                    ctor.setAccessible(true);    
                }
            }
        };

    }
}
