package com.picocontainer.web;

import java.io.Serializable;

import com.picocontainer.behaviors.Storing;
import com.picocontainer.lifecycle.LifecycleState;

@SuppressWarnings("serial")
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
