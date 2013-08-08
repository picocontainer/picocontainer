importPackage(Packages.com.picocontainer)
importPackage(Packages.com.picocontainer.modules)

var pico = parent.makeChildContainer()
		.addComponent("moduleOneTest")
		.addComponent("ServiceOne",com.picocontainer.testmodules.moduleOne.DefaultServiceOne);
	
	new Publisher(pico, parent)
		.publish("ServiceOne");

	
	//var ca = pico.getComponentAdapter("ServiceTwo");
	
	//Publish service in parent container
	//parent.addAdapter(new Publishing(pico, ca));
