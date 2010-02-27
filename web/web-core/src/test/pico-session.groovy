pico = builder.container(parent:parent, scope:assemblyScope) {
	component(key:'sessionScopedInstance', instance:'foo bar')
}