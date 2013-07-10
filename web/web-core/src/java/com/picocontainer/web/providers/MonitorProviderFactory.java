package com.picocontainer.web.providers;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.providers.defaults.NullMonitorProvider;

/**
 * Utility class that helps convert context parameters to a ComponentMonitor.  Current parameters that may be used in constructing
 * providers are:
 * <ul>
 * 	<li>{@link javax.servlet.ServletContext}</li>
 *  <li>{@link java.lang.ClassLoader}</li>
 * </ul>
 * @author Michael Rimov
 */
public class MonitorProviderFactory extends AbstractProviderFactory<ComponentMonitor> {


	/**
	 * Constructs a component monitor based on a comma-delimited list of strings.  All monitors except the last
	 * one must implement {@link com.picocontainer.web.providers.DelegatingAwareMonitorProvider}.  Last one only needs to implement {@link com.picocontainer.web.providers.MonitorProvider}
	 * @param parameterString
	 * @return
	 */
	public ComponentMonitor constructProvider(ServletContext context,String parameterString) {

		List<String> providers = getParameterStrings(parameterString);
		
		if (providers.size() == 0) {
			return new NullMonitorProvider().get();
		}
		
		Collections.reverse(providers);
		ComponentMonitor returnValue = null;
		MutablePicoContainer picoServices = buildParentPico(context);
		for (String eachProviderToInstantiate : providers) {
			DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer(picoServices.getComponent(ClassLoader.class), picoServices, new NullComponentMonitor());
			pico.addComponent(eachProviderToInstantiate, new ClassName(eachProviderToInstantiate));
			
			MonitorProvider provider = (MonitorProvider)pico.getComponent(eachProviderToInstantiate);
			if (returnValue == null) {
				returnValue = provider.get();
			} else {
				if (provider instanceof DelegatingAwareMonitorProvider) {
					DelegatingAwareMonitorProvider castProvider = (DelegatingAwareMonitorProvider)provider;
					returnValue = castProvider.wrap(returnValue);
				} else {
					throw new ProviderSetupException(eachProviderToInstantiate 
							+ " is not a " 
							+ DelegatingAwareMonitorProvider.class.getName() 
							+ ".  It should be the last monitor in the list");
				}
			}
		}
		
		return returnValue;
	}
	
}
