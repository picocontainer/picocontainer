pico = builder.container(parent:parent, scope:assemblyScope) {
	component(key:'applicationScopedInstance', instance:'foo bar')
	component(key:'testFoo', class:'com.picocontainer.web.Foo')
}
