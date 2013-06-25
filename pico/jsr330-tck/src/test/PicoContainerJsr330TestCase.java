import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;
import javax.inject.Provider;

import junit.framework.Test;
import junit.framework.TestCase;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Seatbelt;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.Parameter;
import com.picocontainer.behaviors.AdaptingBehavior;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.containers.JSRPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.AnnotatedMethodInjection;
import com.picocontainer.injectors.Jsr330ConstructorInjection;
import com.picocontainer.monitors.ConsoleComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.JSR330ComponentParameter;

import static com.picocontainer.Characteristics.*;

public class PicoContainerJsr330TestCase extends TestCase {

//    bind(Car.class).to(Convertible.class);
//    bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
//    bind(Engine.class).to(V8Engine.class);
//    bind(Cupholder.class);
//    bind(Tire.class);
//    bind(FuelTank.class);


	@Drivers
	public static class DriverSeatProvider implements Provider<Seat> {
		
		private MutablePicoContainer pico;

		public DriverSeatProvider(MutablePicoContainer pico) {
			this.pico = pico;
		}

		public Seat get() {
	        try {
	            return (Seat)pico.getComponent("theDriversSeat");
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
		}
	}
	
	public static class PlainSeatProvider implements Provider<Seat> {
		
		private MutablePicoContainer pico;

		public PlainSeatProvider(MutablePicoContainer pico) {
			this.pico = pico;
		}

		public Seat get() {
	        try {
	            return (Seat)pico.getComponent("plainSeat");
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
		}
	}
	
	public static class PlainTireProvider implements Provider<Tire> {
		private MutablePicoContainer pico;

		public PlainTireProvider(MutablePicoContainer pico) {
			this.pico = pico;
		}

        public Tire get() {
            return (Tire) pico.getComponent("plainTire");
        }
	}
	
	
    @Named("spare")
	public static class SpareTireProvider implements Provider<Tire> {
    	
		private MutablePicoContainer pico;

		public SpareTireProvider(MutablePicoContainer pico) {
			this.pico = pico;
		}
		
        public Tire get() {
            try {
            	return (Tire)pico.getComponent("spareTire");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }    	
	}
    
    public static class EngineProvider implements Provider<Engine> {
		private MutablePicoContainer pico;

		public EngineProvider(MutablePicoContainer pico) {
			this.pico = pico;
		}
		
		public Engine get() {
			return (Engine)pico.getComponent("engine");
		}
    	
    }
	
	
	public static Test suite() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, NoSuchFieldException {
    	final MutablePicoContainer pico = new JSRPicoContainer(new ConsoleComponentMonitor());
    	
    	DriverSeatProvider driversSeatProvider = new DriverSeatProvider(pico);
    	PlainTireProvider plainTireProvider = new PlainTireProvider(pico);
    	SpareTireProvider spareTireProvider = new SpareTireProvider(pico);
    	PlainSeatProvider plainSeatProvider = new PlainSeatProvider(pico);
    	EngineProvider engineProvider = new EngineProvider(pico);
    	
    	//Allow static injection on all classes since the JSR TCK requires it heavily
    	pico.change(STATIC_INJECTION);
        pico.addComponent(Car.class, Convertible.class)
                .addComponent(FuelTank.class)
                .addComponent(Seatbelt.class)
                .addComponent(Cupholder.class, Cupholder.class)
                .addProvider(driversSeatProvider)
                .addProvider(plainSeatProvider)
                .addProvider(plainTireProvider)
                .addProvider(spareTireProvider)
                .addProvider(engineProvider)
        
                //Components Used By the providers
                .addComponent("plainSeat", Seat.class)
        		.addComponent("theDriversSeat", DriversSeat.class)
                .addComponent("spareTire", SpareTire.class)
                .addComponent("plainTire", Tire.class)
        		.addComponent("engine", V8Engine.class);
                
                
        
        
        
        Car car = pico.getComponent(Car.class);
        return Tck.testsFor(car, true, true);
    }

}