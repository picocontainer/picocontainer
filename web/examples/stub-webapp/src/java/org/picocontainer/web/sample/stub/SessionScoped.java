package org.picocontainer.web.sample.stub;

import java.io.Serializable;

public class SessionScoped implements Serializable {

    private AppScoped appScopeComp;
    private int counter;

    public SessionScoped(AppScoped appScopeComp) {
        this.appScopeComp = appScopeComp;
    }

    public String getCounterAndDependantsCounters() {
        return appScopeComp.getCounter() + "<br/> &nbsp;&nbsp;&nbsp; SessionScoped id: " + System.identityHashCode(this) + ", counter: " + ++counter;
    }

}