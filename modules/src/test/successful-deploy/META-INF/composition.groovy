import org.picocontainer.*;
println("Assembly Scope: " + assemblyScope);

pico = new PicoBuilder().withCaching().withLifecycle().build();
if ("Test".equals(assemblyScope)) {	
	pico.addComponent("Assembly Scope Test");
} else {
	pico.addComponent("Groovy")
		.addComponent("zap", foo.bar.Zap);		
}
