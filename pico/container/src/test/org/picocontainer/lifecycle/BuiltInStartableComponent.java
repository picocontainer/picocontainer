package org.picocontainer.lifecycle;

import org.picocontainer.Startable;
import org.picocontainer.Disposable;

public class BuiltInStartableComponent implements Startable, Disposable {

    StringBuilder sb;

    public BuiltInStartableComponent(StringBuilder sb) {
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

