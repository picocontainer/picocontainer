package org.picocontainer.script.testmodel;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.injectors.AdaptingInjection;

import java.util.Properties;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public final class TestInjection extends AdaptingInjection {

    public final StringBuffer sb;

    public TestInjection(StringBuffer sb) {
        this.sb = sb;
    }

    @SuppressWarnings("unchecked")
    public ComponentAdapter createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                    Properties componentProperties, Object componentKey, Class componentImplementation, Parameter... parameters) throws PicoCompositionException {
        sb.append("called");
        return super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                                            componentProperties, componentKey, componentImplementation, parameters);
    }
}
