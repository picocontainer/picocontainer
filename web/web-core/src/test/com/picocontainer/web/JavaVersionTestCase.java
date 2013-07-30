package com.picocontainer.web;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class JavaVersionTestCase {

    @Test
    public void javaVersionShouldBeJdkSevenOrAboveBecauseServletApiVersionRequiresThatDuringCompilation() {

        String ver = System.getProperty("java.runtime.version");
        assertThat(ver, greaterThanOrEqualTo("1.7"));

    }

}
