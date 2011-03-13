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
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AnnotatedMethodInjection;
import org.picocontainer.injectors.Jsr330Injection;
import org.picocontainer.monitors.NullComponentMonitor;

public class PicoContainerJsr330TestCase extends TestCase {

//    bind(Car.class).to(Convertible.class);
//    bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
//    bind(Engine.class).to(V8Engine.class);
//    bind(Cupholder.class);
//    bind(Tire.class);
//    bind(FuelTank.class);



    public static Test suite() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching(), 
                new Jsr330Injection());
        pico.addComponent(Car.class, Convertible.class)
                .addAdapter(new AnnotatedMethodInjection.AnnotatedMethodInjector(Seat.class, DriversSeat.class, Parameter.DEFAULT, new NullComponentMonitor(), false, Drivers.class))
                .addComponent(FuelTank.class)
                .addComponent(Tire.class)
                .addComponent(Engine.class, V8Engine.class)
                .addComponent(Seatbelt.class)
                .addComponent(Seat.class);
        Car car = pico.getComponent(Car.class);
        return Tck.testsFor(car, true, true);
    }
}