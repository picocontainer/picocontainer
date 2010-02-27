package org.picocontainer.gems.monitors.prefuse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.picocontainer.gems.monitors.ComponentDependencyMonitor.Dependency;

public class DependencySetTestCase {
    int callCount = 0;

    @Test public void testShouldNotAddDuplicates() throws Exception {
        ComponentDependencyListener mockListener = new ComponentDependencyListener(){
            public void addDependency(Dependency dependency) {
             callCount++;
            }    
        };       
        DependencySet set = new DependencySet(mockListener);
        Dependency dependency = new Dependency(Object.class, String.class);
        set.addDependency(dependency);
        set.addDependency(dependency);
        assertEquals(1, set.getDependencies().length);
        assertEquals(dependency, set.getDependencies()[0]);
        assertEquals("Call count should be called once",1,callCount );
    }
}
