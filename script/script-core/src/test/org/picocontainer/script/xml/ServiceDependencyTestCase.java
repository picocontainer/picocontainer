package org.picocontainer.script.xml;

import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.script.xml.XMLContainerBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.SetterInjection;

public class ServiceDependencyTestCase extends AbstractScriptedContainerBuilderTestCase {

    @SuppressWarnings("serial")
    public static class MySetterInjection extends SetterInjection {
        public MySetterInjection() {
        }
    }

    //TODO - make sure that this container builder can supply a LifecycleStrategy.
    //       meaning MySetterInjection can be swapped for SetterInjectionComponentFactory
    @Test public void testCanInstantiateProcessWithSDIDependencies() {
        Reader script = new StringReader("" +
                "<container component-adapter-factory='"+ MySetterInjection.class.getName()+"'>"+
                "  <component-implementation class='"+Service1Impl.class.getName()+"'/>"+
                "  <component-implementation class='"+ServiceAImpl.class.getName()+"'/>"+
                "  <component-implementation class='"+Service2Impl.class.getName()+"'/>"+
                "  <component-implementation class='"+ServiceBImpl.class.getName()+"'/>"+
                "  <component-implementation class='"+Process.class.getName()+"'/>"+
                "</container>");
        assertProcessWithDependencies(script);
    }

    private void assertProcessWithDependencies(Reader script) {
        PicoContainer pico = buildContainer(script);
        assertNotNull(pico);
        Process process = pico.getComponent(Process.class);
        assertNotNull(process);
        assertNotNull(process.getServiceA());
        assertNotNull(process.getServiceA().getService1());
        assertNotNull(process.getServiceB());
        assertNotNull(process.getServiceB().getService2());
    }

    private PicoContainer buildContainer(Reader script) {
        return buildContainer(new XMLContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
    }
    
    public static class Process {
        private ServiceA serviceA;

        private ServiceB serviceB;

        // use with SDI
        public Process() {
        }

        public ServiceA getServiceA() {
            return serviceA;
        }

        public void setServiceA(ServiceA serviceA) {
            this.serviceA = serviceA;
        }

        public ServiceB getServiceB() {
            return serviceB;
        }

        public void setServiceB(ServiceB serviceB) {
            this.serviceB = serviceB;
        }
    }

    public static interface Service1 {
    }

    public static interface Service2 {
    }

    public static class Service1Impl implements Service1 {
        public Service1Impl() {
        }
    }

    public static class Service2Impl implements Service2 {
        public Service2Impl() {
        }
    }

    public static interface ServiceA {
        public Service1 getService1();
    }

    public static interface ServiceB {
        public Service2 getService2();
    }

    public static class ServiceAImpl implements ServiceA {
        private Service1 service1;
        public ServiceAImpl() {
        }
        public Service1 getService1() {
            return service1;
        }
        public void setService1(Service1 service1) {
            this.service1 = service1;
        }        
    }

    public static class ServiceBImpl implements ServiceB {
        private Service2 service2;
        public ServiceBImpl() {
        }
        public Service2 getService2() {
            return service2;
        }
        public void setService2(Service2 service2) {
            this.service2 = service2;
        }        
    }
}

   