/**
 * 
 */
package com.picocontainer.web.providers;

import java.util.List;

import javax.servlet.ServletContext;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;

/**
 * @author Mike
 *
 */
public class LifecycleProviderFactory extends AbstractProviderFactory<LifecycleStrategy> {
	
	private ComponentMonitor monitor;

	public LifecycleProviderFactory(ComponentMonitor monitor) {
		this.monitor = monitor;
		
	}

	@Override
	public LifecycleStrategy constructProvider(ServletContext context, String parameterValue) {
		List<String> providers = getParameterStrings(parameterValue);
		if (providers.size() == 0) {
			return new StartableLifecycleStrategy(monitor);
		}
		
		if (providers.size() > 1) {
			throw new IllegalArgumentException("pico.lifecycleStrategy can only have 1 strategy listed");
		}
		
		MutablePicoContainer picoServices = buildParentPico(context);
		picoServices.addComponent(monitor);
		
		DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer(picoServices.getComponent(ClassLoader.class), picoServices, new NullComponentMonitor());
		pico.addComponent(providers.get(0), new ClassName(providers.get(0)));
				
		LifecycleStrategy lifecycle = (LifecycleStrategy) pico.getComponent(providers.get(0));
		if (lifecycle == null) {
			throw new NullPointerException("result of picoContainer.getComponent(" + providers.get(0) + ")");
		}
		
		return lifecycle;
	}

}
