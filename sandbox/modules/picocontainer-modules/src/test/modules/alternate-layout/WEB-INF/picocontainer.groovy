import com.picocontainer.*;
println("Assembly Scope: " + assemblyScope);

pico = new PicoBuilder().withCaching().withLifecycle().build();
pico.addComponent("Groovy")
	//Will exist in WEB-INF/lib
	.addComponent(org.apache.commons.beanutils.BeanAccessLanguageException)
	
	//Will exist in WEB-INF/classes
	.addComponent("zap", foo.bar.Zap);		
