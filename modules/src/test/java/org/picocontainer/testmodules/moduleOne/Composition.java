/**
 * 
 */
package org.picocontainer.testmodules.moduleOne;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.Publisher;
import org.picocontainer.modules.deployer.AbstractPicoComposer;

public class Composition extends AbstractPicoComposer {

	public Composition() {
		super();
	}

	@Override
	protected MutablePicoContainer populateChildContainer(
			final MutablePicoContainer childContainer,
			final MutablePicoContainer parent, final Object assemblyScope) {

		childContainer.addComponent("moduleOneTest")
			.addComponent(ServiceOne.class.getName(), DefaultServiceOne.class);

		new Publisher(childContainer, parent)
			.publish(ServiceOne.class.getName());
			
		
//		final ComponentAdapter<ServiceOne> ca = childContainer
//				.getComponentAdapter(ServiceOne.class, (NameBinding) null);
//
//		parent.addAdapter(new Publishing<ServiceOne>(childContainer, ca));

		return childContainer;
	}

}
