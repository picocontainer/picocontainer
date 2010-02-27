package org.picocontainer.web;

import org.picocontainer.behaviors.Storing;
import org.picocontainer.lifecycle.DefaultLifecycleState;
import org.picocontainer.lifecycle.LifecycleState;

import java.io.Serializable;

public class SessionStoreHolder implements Serializable {
    private final Storing.StoreWrapper storeWrapper;
    private final LifecycleState lifecycleState;

    public SessionStoreHolder(Storing.StoreWrapper storeWrapper,
                              LifecycleState lifecycleState) {
        this.storeWrapper = storeWrapper;
        this.lifecycleState = lifecycleState;
    }

    Storing.StoreWrapper getStoreWrapper() {
        return storeWrapper;
    }

    LifecycleState getLifecycleState() {
        return lifecycleState;
    }
}
