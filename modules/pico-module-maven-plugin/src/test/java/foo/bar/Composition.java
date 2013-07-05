/**
 * 
 */
package foo.bar;

import com.picocontainer.modules.deployer.AbstractPicoComposer;

import com.picocontainer.MutablePicoContainer;

/**
 * @author Mike
 *
 */
public class Composition extends AbstractPicoComposer {

	/**
	 * Default constructor.
	 */
	public Composition() {
		super();
	}

	/** {@inheritDoc} **/
	@Override
	protected MutablePicoContainer populateChildContainer(
			MutablePicoContainer childContainer, MutablePicoContainer parent,
			Object assemblyScope) {

		childContainer.addComponent("zap", Zap.class)
					  .addComponent("Java");
					
		return childContainer;
	}

}
