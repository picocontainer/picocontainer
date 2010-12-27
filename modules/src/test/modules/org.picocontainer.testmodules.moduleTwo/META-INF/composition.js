importPackage(Packages.org.picocontainer)
importClass(Packages.org.picocontainer.modules.adapter.Publishing)
importPackage(org.picocontainer.classname)

var pico = parent.makeChildContainer()
		.addComponent("moduleOneTest")
		.addComponent("ServiceTwo",org.picocontainer.testmodules.moduleTwo.DefaultServiceTwo);
	
	
	var ca = pico.getComponentAdapter("ServiceTwo");
	
	//Publish service in parent container
	parent.addAdapter(new Publishing(pico, ca));
