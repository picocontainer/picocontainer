import junit.framework.Test;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Seatbelt;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.Jsr330Injection;

public class PicoContainerJsr330Tests {
    public static Test suite() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching(), 
                new Jsr330Injection());
        pico.addComponent(Car.class, Convertible.class)
                .addComponent(DriversSeat.class)
                .addComponent(FuelTank.class)
                .addComponent(Tire.class)
                .addComponent(V8Engine.class)
                .addComponent(Seatbelt.class)
                .addComponent(Seat.class);
        Car car = pico.getComponent(Car.class);
        return Tck.testsFor(car, true, true);
    }
}