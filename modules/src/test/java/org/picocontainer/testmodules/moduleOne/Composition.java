/**
 * 
 */
package org.picocontainer.testmodules.moduleOne;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.modules.adapter.Publishing;
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

		final ComponentAdapter<ServiceOne> ca = childContainer
				.getComponentAdapter(ServiceOne.class, (NameBinding) null);

		parent.addAdapter(new Publishing<ServiceOne>(childContainer, ca));

		return childContainer;
	}

}
