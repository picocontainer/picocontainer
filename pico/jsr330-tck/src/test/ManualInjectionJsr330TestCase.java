import junit.framework.Test;
import junit.framework.TestCase;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.RoundThing;
import org.atinject.tck.auto.accessories.SpareTire;

import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ManualInjectionJsr330TestCase extends TestCase {
    
    public static Test suite() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, NoSuchFieldException {

        Constructor<?> seatCtor = Seat.class.getDeclaredConstructors()[0];
        final Constructor<?> driversSeatCtor = DriversSeat.class.getDeclaredConstructors()[0];
        seatCtor.setAccessible(true);

        final Seat[] plainSeat = new Seat[1];

        Provider<Seat> plainSeatProvider = new Provider<Seat>() {
            public Seat get() {
                return plainSeat[0];
            }
        };

        final Cupholder cupholder = new Cupholder(plainSeatProvider);
        plainSeat[0] = (Seat) seatCtor.newInstance(cupholder);
        final DriversSeat driversSeatA = (DriversSeat) driversSeatCtor.newInstance(cupholder);
        final DriversSeat driversSeatB = (DriversSeat) driversSeatCtor.newInstance(cupholder);

        final FuelTank fuelTank = new FuelTank();

        final Tire plainTire = new Tire(fuelTank);
        tireInjections(plainTire);

        final SpareTire spareTire = new SpareTire(fuelTank, new FuelTank());
        //tireInjections(spareTire);
        spareTireInjections(spareTire);

        Provider<Seat> driversSeatProvider = new Provider<Seat>() {
            public Seat get() {
                try {
                    return (DriversSeat) driversSeatCtor.newInstance(cupholder);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Provider<Tire> plainTireProvider = new Provider<Tire>() {
            public Tire get() {
                return new Tire(fuelTank);
            }
        };

        Provider<Tire> spareTireProvider = new Provider<Tire>() {
            public Tire get() {
                try {
                    SpareTire aSpareTire = new SpareTire(fuelTank, new FuelTank());
                    tireInjections(aSpareTire);
                    spareTireInjections(aSpareTire);
                    return aSpareTire;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        final V8Engine v8Engine = new V8Engine();
        // V8Engine has same method, therefore don't call it in the super class
        //injectMethod(v8Engine, Engine.class, "injectTwiceOverriddenWithOmissionInMiddle");
        //injectIntoMethod(v8Engine, Engine.class, "injectPackagePrivateMethodForOverride");
        // V8Engine has same method, therefore don't call it in the super class
        //injectMethod(v8Engine, Engine.class, "injectPackagePrivateMethod");
        // V8Engine has same method but declared w/o @Inject - not called at all.
        //injectMethod(v8Engine, GasEngine.class, "injectTwiceOverriddenWithOmissionInSubclass");
        injectIntoMethod(v8Engine, V8Engine.class, "injectTwiceOverriddenWithOmissionInMiddle");
        injectIntoMethod(v8Engine, V8Engine.class, "injectPackagePrivateMethod");

        Provider<Engine> engineProvider = new Provider<Engine>() {
            public Engine get() {
                return v8Engine;
            }
        };

        Constructor<?> convertibelCtor = Convertible.class.getDeclaredConstructor(Seat.class, Seat.class, Tire.class, Tire.class,
                Provider.class, Provider.class, Provider.class, Provider.class);
        convertibelCtor.setAccessible(true);
        Car car = (Car) convertibelCtor.newInstance(plainSeat[0], driversSeatA, plainTire, spareTire,
                plainSeatProvider, driversSeatProvider, plainTireProvider, spareTireProvider);

        injectIntoField(car, Convertible.class, "driversSeatA", driversSeatA);
        injectIntoField(car, Convertible.class, "driversSeatB", driversSeatB);
        injectIntoField(car, Convertible.class, "spareTire", spareTire);
        injectIntoField(car, Convertible.class, "cupholder", cupholder);
        injectIntoField(car, Convertible.class, "engineProvider", engineProvider);
        injectIntoField(car, Convertible.class, "fieldPlainSeat", plainSeat[0]);
        injectIntoField(car, Convertible.class, "fieldDriversSeat", driversSeatA);
        injectIntoField(car, Convertible.class, "fieldPlainTire", plainTire);
        injectIntoField(car, Convertible.class, "fieldSpareTire", spareTire);
        injectIntoField(car, Convertible.class, "fieldPlainSeatProvider", plainSeatProvider);
        injectIntoField(car, Convertible.class, "fieldDriversSeatProvider", driversSeatProvider);
        injectIntoField(car, Convertible.class, "fieldPlainTireProvider", plainTireProvider);
        injectIntoField(car, Convertible.class, "fieldSpareTireProvider", spareTireProvider);

        injectIntoMethod(car, Convertible.class, "injectMethodWithZeroArgs");
        injectIntoMethod(car, Convertible.class, "injectMethodWithNonVoidReturn");

        Method injectInstanceMethodWithManyArgs = Convertible.class.getDeclaredMethod("injectInstanceMethodWithManyArgs",
                Seat.class, Seat.class, Tire.class, Tire.class, Provider.class, Provider.class, Provider.class, Provider.class);
        injectInstanceMethodWithManyArgs.setAccessible(true);
        injectInstanceMethodWithManyArgs.invoke(car, plainSeat[0], driversSeatA, plainTire,
                spareTire, plainSeatProvider, driversSeatProvider, plainTireProvider, spareTireProvider);

        return Tck.testsFor(car, false, true);
    }

    private static void spareTireInjections(SpareTire spareTire) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        injectIntoMethod(spareTire, RoundThing.class, "injectPackagePrivateMethod4");

        injectIntoField(spareTire, Tire.class, "fieldInjection", new FuelTank());
        injectIntoMethod(spareTire, Tire.class, "injectPrivateMethod");
        injectIntoMethod(spareTire, Tire.class, "injectPackagePrivateMethod");
        injectIntoMethod(spareTire, Tire.class, "injectPackagePrivateMethod2");
        injectIntoMethod(spareTire, Tire.class, "injectPackagePrivateMethodForOverride");
        injectIntoMethod(spareTire, Tire.class, "injectPrivateMethodForOverride");
        injectIntoMethod(spareTire, Tire.class, "injectPackagePrivateMethod2");
        injectIntoMethod(spareTire, Tire.class, "injectPackagePrivateMethod3");
        injectIntoMethod(spareTire, Tire.class, "supertypeMethodInjection", FuelTank.class, new FuelTank());

        
        injectIntoField(spareTire, SpareTire.class, "fieldInjection", new FuelTank());
        injectIntoMethod(spareTire, SpareTire.class, "subtypeMethodInjection", FuelTank.class, new FuelTank());
        injectIntoMethod(spareTire, SpareTire.class, "injectPrivateMethod");
        injectIntoMethod(spareTire, SpareTire.class, "injectProtectedMethod");
        
        
        injectIntoMethod(spareTire, SpareTire.class, "injectPackagePrivateMethod");
        
        injectIntoMethod(spareTire, SpareTire.class, "injectPackagePrivateMethod2");
        spareTire.injectPublicMethod();
    }

    private static void tireInjections(Tire tire) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        injectIntoMethod(tire, RoundThing.class, "injectPackagePrivateMethod2");
        injectIntoMethod(tire, RoundThing.class, "injectPackagePrivateMethod3");
        injectIntoMethod(tire, RoundThing.class, "injectPackagePrivateMethod4");

        injectIntoField(tire, Tire.class, "fieldInjection", new FuelTank());
        injectIntoMethod(tire, Tire.class, "injectPrivateMethod");
        injectIntoMethod(tire, Tire.class, "injectPackagePrivateMethod");
        injectIntoMethod(tire, Tire.class, "injectPackagePrivateMethod2");
        injectIntoMethod(tire, Tire.class, "injectPackagePrivateMethod3");
        injectIntoMethod(tire, Tire.class, "injectPrivateMethodForOverride");
        injectIntoMethod(tire, Tire.class, "injectPackagePrivateMethodForOverride");
        
        
        injectIntoMethod(tire, Tire.class, "injectPackagePrivateMethodForOverride");
        injectIntoMethod(tire, Tire.class, "injectProtectedMethodForOverride");
        injectIntoMethod(tire, Tire.class, "injectPublicMethodForOverride");
        injectIntoMethod(tire, Tire.class, "supertypeMethodInjection", FuelTank.class, new FuelTank());

        
    }

    private static void injectIntoField(Object inst, Class<?> type, String name, Object param) throws NoSuchFieldException, IllegalAccessException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(inst, param);
    }

    private static void injectIntoMethod(Object inst, Class<?> type, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        injectIntoMethod(inst, type, name, new Class[0], new Object[0]);
    }

    private static void injectIntoMethod(Object inst, Class<?> type, String name, Class<?> pType, Object param) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        injectIntoMethod(inst, type, name, new Class[]{pType}, new Object[]{param});
    }

    private static void injectIntoMethod(Object inst, Class<?> type, String name, Class<?>[] pTypes, Object[] params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = type.getDeclaredMethod(name, pTypes);
        method.setAccessible(true);
        method.invoke(inst, params);
    }
}