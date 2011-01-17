/**
 * 
 */
package foo.bar;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.modules.deployer.AbstractPicoComposer;

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
