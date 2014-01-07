import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoContainer;

import play.Application;
import play.GlobalSettings;


/**
 * Registers the PicoContainer 
 * @author Michael Rimov
 *
 */
public class Global extends GlobalSettings {
	
	/**
	 * The one and only PicoContainer
	 */
	private MutablePicoContainer pico;

	
	/**
	 * This is the method that performs what you need -- every controller class
	 * is looked up and instantiated in Pico.
	 */
	 @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
		 assert pico != null;
		 return pico.getComponent(controllerClass);
    }



	 /**
	  * Called when the app starts up.  Compose the container here.
	  */
	@Override
	public void onStart(Application app) {
		super.onStart(app);

		if (pico != null) {
			pico.stop();
			pico.dispose();
			pico = null;
		}
		
		pico = new PicoBuilder().withCaching().withLifecycle().build();
		new Composition().compose(pico);
		pico.start();
	}


	/**
	 * Called when the app stops, stop and dispose the container here.
	 */
	@Override
	public void onStop(final Application app) {

		if (pico != null) {
			pico.stop();
			pico.dispose();
		}
		
		super.onStop(app);
	}	

}
