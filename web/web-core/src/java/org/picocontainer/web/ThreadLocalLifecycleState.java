package org.picocontainer.web;

import org.picocontainer.lifecycle.LifecycleState;
import org.picocontainer.lifecycle.DefaultLifecycleState;

public class ThreadLocalLifecycleState implements LifecycleState {

    private LifecycleStateThreadLocal tl = new LifecycleStateThreadLocal();

    public void removingComponent() {
        tl.get().removingComponent();
    }

    public void starting(String containerName) {
        tl.get().starting(containerName);
    }

    public void stopping(String containerName) {
        tl.get().stopping(containerName);
    }

    public void stopped() {
        tl.get().stopped();
    }

    public boolean isStarted() {
        return tl.get().isStarted();
    }

    public boolean isStopped() {
        return tl.get().isStopped();
    }

    public boolean isDisposed() {
        return tl.get().isDisposed();
    }

    public void disposing(String containerName) {
        tl.get().disposing(containerName);
    }

    public void disposed() {
        tl.get().disposed();
    }

    public void putLifecycleStateModelForThread(LifecycleState lifecycleState) {
        tl.set(lifecycleState);
    }

    public LifecycleState resetStateModelForThread() {
        DefaultLifecycleState dls = new DefaultLifecycleState();
        tl.set(dls);
        return dls;
    }

    public void invalidateStateModelForThread() {
        tl.set(null);
    }

    private static class LifecycleStateThreadLocal extends ThreadLocal<LifecycleState> {
        protected LifecycleState initialValue() {
            return new DefaultLifecycleState();
        }
    }

}
