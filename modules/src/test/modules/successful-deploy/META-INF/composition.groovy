import org.picocontainer.*;


pico = new PicoBuilder().withCaching().withLifecycle().build();
if ("Test".equals(assemblyScope)) {	
	pico.addComponent("Assembly Scope Test");
} else {
	pico.addComponent("Groovy")
		.addComponent("zap", foo.bar.Zap);		
}
