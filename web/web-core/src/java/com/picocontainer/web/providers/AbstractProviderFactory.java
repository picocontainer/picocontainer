package com.picocontainer.web.providers;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;

abstract public class AbstractProviderFactory<T> {


	protected MutablePicoContainer buildParentPico(ServletContext context) {
		MutablePicoContainer parent = new PicoBuilder().withCaching().withLifecycle().build();
		parent.addComponent(context);
		
		ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
			public ClassLoader run() {
				return Thread.currentThread().getContextClassLoader();
			}}
		);
		parent.addComponent(cl);
		
		return parent;
	}

	protected List<String> getParameterStrings(String parameterString) {
		List<String> providers = new ArrayList<String>();
		if (parameterString == null || parameterString.length() == 0) {
			return providers;
		}
		
		for (StringTokenizer stok = new StringTokenizer(parameterString, ","); stok.hasMoreTokens();){
			providers.add(stok.nextToken());
		}
		return providers;
	}
	
	abstract public T constructProvider(ServletContext context, String parameterValue);

}
