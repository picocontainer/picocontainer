import model.CheeseDao;
import model.CheeseService;
import model.DefaultCheeseService;
import model.InMemoryCheeseDao;

import com.picocontainer.MutablePicoContainer;

import controllers.CheeseController;


/**
 * Put all the Composition stuff in here.
 * @author Michael Rimov
 */
public final class Composition {

	public void compose(final MutablePicoContainer pico) {
		
		//Wire in the services.
		pico.addComponent(CheeseDao.class, InMemoryCheeseDao.class)
			.addComponent(CheeseService.class, DefaultCheeseService.class);
		
		
		//Wire in the controller
		pico.addComponent(CheeseController.class);
	}
	
}
