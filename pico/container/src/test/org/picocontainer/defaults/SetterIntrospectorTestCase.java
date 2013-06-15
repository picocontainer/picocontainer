/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults;

import java.beans.IntrospectionException;

import org.junit.Test;

/**
 * @author Aslak Helles&oslash;y
 */
public class SetterIntrospectorTestCase {
    public static class TestBean {
        public void setPublicMethod(final int i) {
        }

        public void setPublicMETHODAgain(final int i) {
        }

        public void setMOOky(final int i) {
        }

        public void setFOOBAR(final int i) {
        }

        public void set(final int i) {
        }

        public void sets(final int i) {
        }

        public void fooBar(final int i) {
        }

        public void setX(final int i) {
        }

        public static void setStaticMethod(final int i) {
        }

        public static void setMany() {
        }

        protected void setProtectedMethod(final int i) {
        }

        private void setPrivateMethod(final int i) {
        }
    }

    @Test public void testShouldConvertPropertyNamesInSameWayAsBeanInfo() throws IntrospectionException {

// TODO - to test via SetterInjectionComponentAdaptor with mock/expects.

//        BeanInfo beanInfo = Introspector.getBeanInfo(TestBean.class);
//        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
//
//        Map setters = getSetters(TestBean.class);
//        assertEquals(propertyDescriptors.length, setters.size());
//
//        for (int i = 0; i < propertyDescriptors.length; i++) {
//            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
//            String expectedPropertyName = propertyDescriptor.getName();
//            assertEquals("No property found for " + expectedPropertyName, propertyDescriptor.getWriteMethod(), setters.get(expectedPropertyName));
//        }
    }

}
