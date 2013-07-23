package com.picocontainer.web.providers;

import java.util.List;

import javax.servlet.ServletContext;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.ContextParameters;
import com.picocontainer.web.providers.defaults.DefaultParentPicoProvider;

public class ParentPicoProviderFactory extends AbstractProviderFactory<ParentPicoProvider> {

	@Override
	public ParentPicoProvider constructProvider(ServletContext context, String parameterValue) {
		if (parameterValue == null || parameterValue.trim().length() == 0) {
			return new DefaultParentPicoProvider();
		}

		MutablePicoContainer picoServices = buildParentPico(context);
		
		List<String> providers = getParameterStrings(parameterValue);
		if (providers.size() > 1) {
			throw new IllegalArgumentException(ContextParameters.PARENT_PICO +  " can only have 1 strategy listed");
		}
				
		
		DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer(picoServices.getComponent(ClassLoader.class), picoServices, new NullComponentMonitor());
		pico.addComponent(providers.get(0), new ClassName(providers.get(0)));
				
		ParentPicoProvider parentProvider = (ParentPicoProvider) pico.getComponent(providers.get(0));
		if (parentProvider == null) {
			throw new NullPointerException("result of picoContainer.getComponent(" + providers.get(0) + ")");
		}
		
		return parentProvider;
	}

}
