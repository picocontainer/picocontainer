package org.picocontainer.script.testmodel;

import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public final class TestInjection extends AdaptingInjection {

    public final StringBuffer sb;

    public TestInjection(StringBuffer sb) {
        this.sb = sb;
    }

    @Override
    public  <T> ComponentAdapter<T>  createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle,
                    Properties componentProps, Object key, Class<T> impl, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException {
        sb.append("called");
        return super.createComponentAdapter(monitor, lifecycle,
                                            componentProps, key, impl, constructorParams, fieldParams, methodParams);
    }
}
