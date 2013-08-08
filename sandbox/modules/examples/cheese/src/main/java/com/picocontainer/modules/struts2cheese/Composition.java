package com.picocontainer.modules.struts2cheese;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.modules.AbstractPicoComposer;
import com.picocontainer.modules.Publisher;
import com.picocontainer.web.sample.struts2.*;

public class Composition extends AbstractPicoComposer {

	public Composition() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected MutablePicoContainer populateChildContainer(MutablePicoContainer pico,
			MutablePicoContainer parent, Object assemblyScope) {
		
		pico.addComponent(Brand.class)
			.addComponent(CheeseDao.class, InMemoryCheeseDao.class)
			.addComponent(CheeseService.class, DefaultCheeseService.class);
		
		
		new Publisher(pico, parent).publish(CheeseService.class);
		
		return pico;
	}

}
