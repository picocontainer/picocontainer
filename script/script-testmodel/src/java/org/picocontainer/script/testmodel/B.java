package org.picocontainer.script.testmodel;

import junit.framework.Assert;


/**
 * @author Mauro Talevi
 */
public final class B extends X {
    public final A a;

    public B(A a) {
        Assert.assertNotNull(a);
        this.a = a;
    }

    public A getA() {
        return a;
    }
}
