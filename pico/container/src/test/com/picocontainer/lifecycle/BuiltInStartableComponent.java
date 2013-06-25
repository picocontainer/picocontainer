package com.picocontainer.lifecycle;

import com.picocontainer.Disposable;
import com.picocontainer.Startable;

public class BuiltInStartableComponent implements Startable, Disposable {

    StringBuilder sb;

    public BuiltInStartableComponent(final StringBuilder sb) {
        this.sb = sb;
    }

    public void start() {
        sb.append("<");
    }

    public void stop() {
        sb.append(">");
    }

    public void dispose() {
        sb.append("!");
    }
}

