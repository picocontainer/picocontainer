import junit.framework.Test;
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
import org.atinject.tck.auto.accessories.SpareTire;

import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StoneageSimulationJsr330Tests {
    
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
        tireInjections(spareTire);
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
        injectMethod(v8Engine, Engine.class, "injectPackagePrivateMethodForOverride");
        // V8Engine has same method, therefore don't call it in the super class
        //injectMethod(v8Engine, Engine.class, "injectPackagePrivateMethod");
        // V8Engine has same method but declared w/o @Inject - not called at all.
        //injectMethod(v8Engine, GasEngine.class, "injectTwiceOverriddenWithOmissionInSubclass");
        injectMethod(v8Engine, V8Engine.class, "injectTwiceOverriddenWithOmissionInMiddle");
        injectMethod(v8Engine, V8Engine.class, "injectPackagePrivateMethod");

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

        injectField(car, Convertible.class, "driversSeatA", driversSeatA);
        injectField(car, Convertible.class, "driversSeatB", driversSeatB);
        injectField(car, Convertible.class, "spareTire", spareTire);
        injectField(car, Convertible.class, "cupholder", cupholder);
        injectField(car, Convertible.class, "engineProvider", engineProvider);
        injectField(car, Convertible.class, "fieldPlainSeat", plainSeat[0]);
        injectField(car, Convertible.class, "fieldDriversSeat", driversSeatA);
        injectField(car, Convertible.class, "fieldPlainTire", plainTire);
        injectField(car, Convertible.class, "fieldSpareTire", spareTire);
        injectField(car, Convertible.class, "fieldPlainSeatProvider", plainSeatProvider);
        injectField(car, Convertible.class, "fieldDriversSeatProvider", driversSeatProvider);
        injectField(car, Convertible.class, "fieldPlainTireProvider", plainTireProvider);
        injectField(car, Convertible.class, "fieldSpareTireProvider", spareTireProvider);

        injectMethod(car, Convertible.class, "injectMethodWithZeroArgs");
        injectMethod(car, Convertible.class, "injectMethodWithNonVoidReturn");

        Method injectInstanceMethodWithManyArgs = Convertible.class.getDeclaredMethod("injectInstanceMethodWithManyArgs",
                Seat.class, Seat.class, Tire.class, Tire.class, Provider.class, Provider.class, Provider.class, Provider.class);
        injectInstanceMethodWithManyArgs.setAccessible(true);
        injectInstanceMethodWithManyArgs.invoke(car, plainSeat[0], driversSeatA, plainTire,
                spareTire, plainSeatProvider, driversSeatProvider, plainTireProvider, spareTireProvider);

        return Tck.testsFor(car, false, true);
    }

    private static void spareTireInjections(SpareTire spareTire) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        injectField(spareTire, SpareTire.class,"fieldInjection", new FuelTank());
        injectMethod(spareTire, SpareTire.class, "subtypeMethodInjection", FuelTank.class, new FuelTank());
        injectMethod(spareTire, SpareTire.class, "injectPrivateMethod");
        injectMethod(spareTire, SpareTire.class, "injectProtectedMethod");
        injectMethod(spareTire, SpareTire.class, "injectPackagePrivateMethod");
        injectMethod(spareTire, SpareTire.class, "injectPackagePrivateMethodForOverride");
        spareTire.injectPublicMethod();
    }

    private static void tireInjections(Tire tire) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        injectField(tire, Tire.class,"fieldInjection", new FuelTank());
        injectMethod(tire, Tire.class, "injectPrivateMethod");
        injectMethod(tire, Tire.class, "injectPackagePrivateMethod");
        injectMethod(tire, Tire.class, "injectPrivateMethodForOverride");
        injectMethod(tire, Tire.class, "injectPackagePrivateMethodForOverride");
        injectMethod(tire, Tire.class, "injectProtectedMethodForOverride");
        injectMethod(tire, Tire.class, "injectPublicMethodForOverride");
        injectMethod(tire, Tire.class, "supertypeMethodInjection", FuelTank.class, new FuelTank());
    }

    private static void injectField(Object inst, Class<?> type, String name, Object param) throws NoSuchFieldException, IllegalAccessException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(inst, param);
    }

    private static void injectMethod(Object inst, Class<?> type, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        injectMethod(inst, type, name, new Class[0], new Object[0]);
    }

    private static void injectMethod(Object inst, Class<?> type, String name, Class<?> pType, Object param) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        injectMethod(inst, type, name, new Class[] {pType}, new Object[]{param});
    }

    private static void injectMethod(Object inst, Class<?> type, String name, Class<?>[] pTypes, Object[] params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = type.getDeclaredMethod(name, pTypes);
        method.setAccessible(true);
        method.invoke(inst, params);
    }
}