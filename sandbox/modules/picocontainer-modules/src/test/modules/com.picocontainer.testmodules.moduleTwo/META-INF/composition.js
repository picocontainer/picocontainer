importPackage(Packages.com.picocontainer)
importPackage(Packages.com.picocontainer.modules)

var pico = parent.makeChildContainer()
		.addComponent("moduleOneTest")
		.addComponent("ServiceTwo",com.picocontainer.testmodules.moduleTwo.DefaultServiceTwo);
	
	new Publisher(pico, parent)
		.publish("ServiceTwo");

	
	//var ca = pico.getComponentAdapter("ServiceTwo");
	
	//Publish service in parent container
	//parent.addAdapter(new Publishing(pico, ca));
