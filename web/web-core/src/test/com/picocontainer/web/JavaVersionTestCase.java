package com.picocontainer.web;

import org.junit.Test;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class JavaVersionTestCase {

    @Test
    public void javaVersionShouldBeJdkSevenOrAboveBecauseServletApiVersionRequiresThatDuringCompilation() {

        String ver = System.getProperty("java.runtime.version");
        assertThat(ver, startsWith("1.7"));

    }

}
