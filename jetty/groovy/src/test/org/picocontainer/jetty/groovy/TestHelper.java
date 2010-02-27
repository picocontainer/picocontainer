package org.picocontainer.jetty.groovy;

import java.io.File;

public class TestHelper {

    public static File getTestWarFile() {
        String testcompJarProperty = System.getProperty("testwar.war");
        if (testcompJarProperty != null) {
            return new File(testcompJarProperty);
        }

        File base = new File(TestHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().getParentFile();

        File coreJettyBase = new File(base.getParent() + File.separator + "core");
        
        File warfile = new File(coreJettyBase,"src" + File.separator + "test" + File.separator + "testwar.war");

        if (!warfile.exists()) {
            warfile = new File(warfile.getAbsolutePath().replaceAll("/container/", "/webcontainer/"));
        }
        return warfile;
    }


}
