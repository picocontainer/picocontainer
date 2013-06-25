def pico = parent.makeChildContainer()
			.addComponent("moduleOneTest")
			.addComponent(ServiceTwo.class, DefaultServiceTwo.class);

final ComponentAdapter<ServiceTwo> ca = childContainer
		.getComponentAdapter(ServiceTwo.class, (NameBinding) null);

//Publish service in parent container
parent.addAdapter(new Publishing<ServiceTwo>(childContainer, ca));