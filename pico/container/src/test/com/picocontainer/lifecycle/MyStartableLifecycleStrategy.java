package com.picocontainer.lifecycle;

import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;

public class MyStartableLifecycleStrategy extends StartableLifecycleStrategy {
        public MyStartableLifecycleStrategy() {
            super(new NullComponentMonitor());
        }

        @Override
		protected String getStopMethodName() {
            return "sstop";
        }

        @Override
		protected String getStartMethodName() {
            return "sstart";
        }

        @Override
		protected String getDisposeMethodName() {
            return "ddispose";
        }


        @Override
		protected Class getStartableInterface() {
            return ThirdPartyStartable.class;
        }

        @Override
		protected Class getDisposableInterface() {
            return ThirdPartyStartable.class;
        }
    }

