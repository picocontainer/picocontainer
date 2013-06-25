package com.picocontainer.script;

import java.io.File;

public class TestHelper {

    public static File getTestCompJarFile() {
        String testcompJarProperty = System.getProperty("testcomp.jar");
        if (testcompJarProperty != null) {
            return new File(testcompJarProperty);
        }

        Class<TestHelper> aClass = TestHelper.class;
        File base = new File(aClass.getProtectionDomain().getCodeSource().getLocation().getFile());

        File tj = new File(base,"src/test-comp/TestComp.jar");
        while (!tj.exists()) {
            base = base.getParentFile();
            if (base == null) {
            	throw new NullPointerException("Could not find testcomp.jar");
            }
            tj = new File(base,"src/test-comp/TestComp.jar");
        }
        return tj;
    }


}
