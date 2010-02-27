import junit.framework.Test;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.*;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

public class PicoContainerJsr330Tests {
    public static Test suite() {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Car.class, Convertible.class)
                .addComponent(DriversSeat.class)
                .addComponent(FuelTank.class)
                .addComponent(Tire.class)
                .addComponent(V8Engine.class)
                .addComponent(Seatbelt.class);
        Car car = pico.getComponent(Car.class);
        return Tck.testsFor(car, true, true);
    }
}