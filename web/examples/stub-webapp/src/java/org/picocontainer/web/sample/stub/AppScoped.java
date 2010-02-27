package org.picocontainer.web.sample.stub;

import java.io.Serializable;

public class AppScoped implements Serializable {

    private int counter;

    public AppScoped() {
    }

    public String getCounter() {
        return "<br/> &nbsp;&nbsp;&nbsp; AppScoped id:" + System.identityHashCode(this) + ", counter: "+ ++counter;
    }
}
