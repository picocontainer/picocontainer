package org.picocontainer.booter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.util.HashMap;
import java.util.Map;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.security.CustomPermissionsURLClassLoader;
import org.picocontainer.DefaultPicoContainer;

public class BrownBear implements Startable {

    private MutablePicoContainer subContainer;

    public BrownBear(Honey honey) throws NoSuchMethodException, IllegalAccessException, InstantiationException, MalformedURLException, ClassNotFoundException {

        try {
            new Socket("google.com", 80);
            System.out.println("BrownBear: 'socket open' NOT blocked to google.com:80 (wrong)");
        } catch (AccessControlException e) {
            System.out.println("BrownBear: 'socket open' blocked to google.com:80 (correct)");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            new Socket("yahoo.com", 80);
            System.out.println("BrownBear: 'socket open' NOT blocked to yahoo.com:80 (correct)");
        } catch (AccessControlException e) {
            e.printStackTrace();
            System.out.println("BrownBear: 'socket open' blocked to yahoo.com:80 (wrong)");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //try {
        //    param2.getClass().getClassLoader();
        //    System.out.println("BrownBear: Can access classloader of class *not* in BrownBear's tree (wrong)");
        //} catch (AccessControlException e) {
        //    System.out.println("BrownBear: Can't access classloader of class *not* in BrownBear's tree (correct)");
        //}

        this.getClass().getClassLoader();
        System.out.println("BrownBear: Can access classloader of this class (correct)");

        System.out.println("BrownBear: I have eaten " + honey.eatSome() + " calories of Honey (of unknown type)");
        Class clazz = null;
        try {
            clazz = this.getClass().getClassLoader().loadClass("org.picocontainer.boot.BeeHiveHoney");
        } catch (ClassNotFoundException cnfe) {
        }
        System.out.println("BrownBear: Can see class for BeeHiveHoney ? - " + (clazz != null));
        System.out.println("BrownBear: honey instance's class type - " + honey.getClass());
        Method nonInterfaceMethod = null;
        try {
            nonInterfaceMethod = honey.getClass().getMethod("nonInterfaceMethod");
        } catch (NoSuchMethodException exception) {
        }
        System.out.println("BrownBear: Can see honey instance's 'nonInterfaceMethod'? - " + (nonInterfaceMethod != null));
        boolean invoked = false;
        try {
            nonInterfaceMethod.invoke(honey);
            invoked = true;
        } catch (Exception e) {
        }
        System.out.println("BrownBear: Can invoke honey instance's 'nonInterfaceMethod'? - " + invoked);
        nonInterfaceMethod = null;
        if (clazz != null) {
            nonInterfaceMethod = clazz.getMethod("nonInterfaceMethod");
        }
        System.out.println("BrownBear: Can see HoneyBeeHoney.nonInterfaceMethod()? - " + (nonInterfaceMethod != null));
        invoked = false;
        try {
            nonInterfaceMethod.invoke(honey);
            invoked = true;
        } catch (Exception e) {
        }
        System.out.println("BrownBear: Can invoke HoneyBeeHoney class' 'nonInterfaceMethod' against honey's instance? - " + invoked);
        System.out.println("BrownBear: Can leverage any implementation detail from honey instance? - false");

        subContainer = new DefaultPicoContainer();
        subContainer.addComponent(Map.class, HashMap.class);
        subContainer.addComponent(PicoContainer.class, DefaultPicoContainer.class);
        subContainer.getComponent(Map.class);
        subContainer.getComponent(PicoContainer.class);

        System.out.println("BrownBear: Can instantiate new DefaultPicoContainer (sub-container)");

        try {
            new BrownBearHelper();
            System.out.println("BrownBear: Can instantiate new DefaultPicoContainer (sub-container) - wrong, DefaultPicoContainer should not be in the classpath");
        } catch (NoClassDefFoundError e) {
            System.out.println("BrownBear: Cannot instantiate new DefaultPicoContainer (sub-container) - correct, DefaultPicoContainer is not in the classpath");
        }

        String qdox = "http://www.ibiblio.org/maven/qdox/jars/qdox-1.5.jar";
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {new URL(qdox)} );
            Class qdoxClass = urlClassLoader.loadClass("com.thoughtworks.qdox.JavaDocBuilder");
            qdoxClass.newInstance();
            System.out.println("BrownBear: Can instantiate new URLClassLoader (incorrect)");
        } catch (AccessControlException e) {
            System.out.println("BrownBear: Cannot instantiate new URLClassLoader (correct)");
        }

        Map permissionsMap = new HashMap();
        permissionsMap.put(qdox, new AllPermission());

        try {
            URLClassLoader urlClassLoader = new CustomPermissionsURLClassLoader(new URL[] {new URL(qdox)}, permissionsMap, this.getClass().getClassLoader() );
            Class qdoxClass = urlClassLoader.loadClass("com.thoughtworks.qdox.JavaDocBuilder");
            qdoxClass.newInstance();
            System.out.println("BrownBear: Can instantiate new CustomPermissionsURLClassLoader (incorrect)");
        } catch (AccessControlException e) {
            System.out.println("BrownBear: Cannot instantiate new CustomPermissionsURLClassLoader (correct)");
        }

    }

    public void start() {
        subContainer.start();
        System.out.println("BrownBear:  Started sub-container");
    }

    public void stop() {
        subContainer.stop();
    }
}