package com.picocontainer.script.testmodel;

import java.util.Properties;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

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
