package org.picocontainer.jetty;

import java.io.File;

public class TestHelper {

    public static File getTestWarFile() {
        String testcompJarProperty = System.getProperty("testwar.war");
        if (testcompJarProperty != null) {
            return new File(testcompJarProperty);
        }

        File base = new File(TestHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().getParentFile();
        File warfile = new File(base,"src" + File.separator + "test" + File.separator + "testwar.war");

        if (!warfile.exists()) {
            warfile = new File(warfile.getAbsolutePath().replaceAll("/container/", "/webcontainer/"));
        }
        return warfile;
    }


}
