package com.picocontainer.web.providers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.picocontainer.ComponentFactory;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.monitors.NullComponentMonitor;

public class ComponentFactoryProviders extends AbstractProviderFactory<ComponentFactory[]> {

	@Override
	public ComponentFactory[] constructProvider(ServletContext context, String parameterValue) {
		List<String> providers = getParameterStrings(parameterValue);
		
		if (providers.size() == 0) {
			return new ComponentFactory[0];
		}
		
		List<ComponentFactory> returnValue = new ArrayList<ComponentFactory>();
		MutablePicoContainer picoServices = buildParentPico(context);
		for (String eachBehaviorToInstantiate : providers) {
			DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer(picoServices.getComponent(ClassLoader.class), picoServices, new NullComponentMonitor());
			pico.addComponent(eachBehaviorToInstantiate, new ClassName(eachBehaviorToInstantiate));
			
			ComponentFactory caf = (ComponentFactory)pico.getComponent(eachBehaviorToInstantiate);
			if (caf == null) {
				throw new NullPointerException("Result of picoContainer.getComponent(" + eachBehaviorToInstantiate + ")");
			}
			
			returnValue.add(caf);
		}
		
		return returnValue.toArray(new ComponentFactory[returnValue.size()]);
	}

}
