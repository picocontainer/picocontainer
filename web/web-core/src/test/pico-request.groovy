pico = builder.container(parent:parent, scope:assemblyScope) {
	component(key:'requestScopedInstance', instance:'foo bar')
	component(key:'testFooHierarchy', class:'com.picocontainer.web.FooHierarchy')
}