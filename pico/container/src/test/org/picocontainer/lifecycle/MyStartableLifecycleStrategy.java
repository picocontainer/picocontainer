package org.picocontainer.lifecycle;

import org.picocontainer.monitors.NullComponentMonitor;

public class MyStartableLifecycleStrategy extends StartableLifecycleStrategy {
        public MyStartableLifecycleStrategy() {
            super(new NullComponentMonitor());
        }

        protected String getStopMethodName() {
            return "sstop";
        }

        protected String getStartMethodName() {
            return "sstart";
        }

        protected String getDisposeMethodName() {
            return "ddispose";
        }


        protected Class getStartableInterface() {
            return ThirdPartyStartable.class;
        }

        protected Class getDisposableInterface() {
            return ThirdPartyStartable.class;
        }
    }

