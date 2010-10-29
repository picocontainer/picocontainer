pico = builder.container(parent:parent) {
	println("Assembly Scope: " + assemblyScope);
	if ("Test".equals(assemblyScope)) {	
		component(instance:'Assembly Scope Test')
	} else {
		component(instance:'Groovy')
		component(key:'zap', class:foo.bar.Zap)
    }
}
