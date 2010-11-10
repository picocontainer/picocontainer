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
	 * 
	 */
	public Composition() {
	}

	/* (non-Javadoc)
	 * @see org.picocontainer.modules.deployer.AbstractPicoComposer#populateChildContainer(org.picocontainer.MutablePicoContainer, org.picocontainer.MutablePicoContainer, java.lang.Object)
	 */
	@Override
	protected MutablePicoContainer populateChildContainer(
			MutablePicoContainer childContainer, MutablePicoContainer parent,
			Object assemblyScope) {
		// TODO Auto-generated method stub
		return null;
	}

}
