/*****************************************************************************
 * Copyright (C) 2003-2012 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.picocontainer.parameters.ComponentParameter.DEFAULT;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.junit.Ignore;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.parameters.BeanParameters;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.parameters.MethodParameters;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.PersonBean;
import org.picocontainer.testmodel.PurseBean;
import org.picocontainer.containers.EmptyPicoContainer;


@SuppressWarnings("serial")
public class SetterInjectorTestCase
    extends AbstractComponentAdapterTest {

    protected Class getComponentAdapterType() {
        return SetterInjection.SetterInjector.class;
    }

    protected ComponentFactory createDefaultComponentFactory() {
        return new Caching().wrap(new SetterInjection());
    }

    protected ComponentAdapter prepDEF_verifyWithoutDependencyWorks(MutablePicoContainer picoContainer) {
        return new SetterInjection.SetterInjector(PersonBean.class, PersonBean.class, new NullComponentMonitor(), "set", false, "", false, 
        		new MethodParameters[] {new BeanParameters("name",new ConstantParameter(
                "Pico Container"))});
    }

    protected ComponentAdapter prepDEF_verifyDoesNotInstantiate(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        return new SetterInjection.SetterInjector(DeadBody.class, DeadBody.class, new NullComponentMonitor(), "set", false, "", false, null
       );
    }

    protected ComponentAdapter prepDEF_visitable() {
        return new SetterInjection.SetterInjector(PersonBean.class, 
        		PersonBean.class, 
        		new NullComponentMonitor(), 
        		"set", 
        		false, 
        		"", 
        		false,
        		new MethodParameters[] {
        			new BeanParameters("name",new ConstantParameter("Pico Container"))
        		});

    }

    protected ComponentAdapter prepSER_isSerializable(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        return new SetterInjection.SetterInjector(PersonBean.class, PersonBean.class, 
        		new NullComponentMonitor(), "set", false, "", false, null
       );
    }

    protected ComponentAdapter prepSER_isXStreamSerializable(MutablePicoContainer picoContainer) {
        return prepSER_isSerializable(picoContainer);
    }

    protected ComponentAdapter prepDEF_isAbleToTakeParameters(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        picoContainer.addComponent(PersonBean.class);
        SetterInjection.SetterInjector componentAdapter = new SetterInjection.SetterInjector(
                PurseBean.class, MoneyPurse.class, new NullComponentMonitor(), "set", false, "", false,
                new MethodParameters[] {
	                new MethodParameters("setOwner", Parameter.DEFAULT),
	                new MethodParameters("setMoney", new ConstantParameter(100.0))
	             }
       );
        return picoContainer.as(Characteristics.NO_CACHE).addAdapter(componentAdapter).getComponentAdapter(PurseBean.class, (NameBinding) null);
    }

    public static class MoneyPurse
            extends PurseBean {
        double money;

        public double getMoney() {
            return money;
        }

        public void setMoney(double money) {
            this.money = money;
        }
    }

    /**
     * @todo this will fail atm because if parameters aren't provided, then default injection
     * is used.
     */
    protected ComponentAdapter prepVER_verificationFails(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        picoContainer.addComponent(PersonBean.class);
        SetterInjection.SetterInjector componentAdapter = new SetterInjection.SetterInjector(
                PurseBean.class, MoneyPurse.class, new NullComponentMonitor(), "set", false, "", false, 
                new MethodParameters[] {
	                new MethodParameters("setOwner", Parameter.DEFAULT),
	             }
          );
        return picoContainer.addAdapter(componentAdapter).getComponentAdapter(PurseBean.class, (NameBinding) null);
    }

    protected ComponentAdapter prepINS_createsNewInstances(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        return new SetterInjection.SetterInjector(PersonBean.class, PersonBean.class, new NullComponentMonitor(), "set", false, "", false,
                new MethodParameters[] {
            		new MethodParameters("setName", Parameter.DEFAULT),
         	}
       );
    }

    public static class Ghost
            extends PersonBean {
        public Ghost() {
            throw new VerifyError("test");
        }
    }

    protected ComponentAdapter prepINS_errorIsRethrown(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        return new SetterInjection.SetterInjector(Ghost.class, Ghost.class, new NullComponentMonitor(), "set", false, "", false,
                new MethodParameters[] {
            		new MethodParameters("setName", Parameter.DEFAULT),
         	}
       );
    }

    public static class DeadBody
            extends PersonBean {
        public DeadBody() {
            throw new RuntimeException("test");
        }
    }

    protected ComponentAdapter prepINS_runtimeExceptionIsRethrown(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        return new SetterInjection.SetterInjector(DeadBody.class, DeadBody.class, new NullComponentMonitor(), "set", false, "", false, 
                new MethodParameters[] {
            		new MethodParameters("setName", Parameter.DEFAULT),
         	}
       );
    }

    public static class HidingPersion
            extends PersonBean {
        public HidingPersion() throws Exception {
            throw new Exception("test");
        }
    }

    protected ComponentAdapter prepINS_normalExceptionIsRethrownInsidePicoInitializationException(
            MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        return new SetterInjection.SetterInjector(
                HidingPersion.class, HidingPersion.class, new NullComponentMonitor(), "set", false, "", false, 
                new MethodParameters[] {
            		new BeanParameters("name", Parameter.DEFAULT),
         	}
       );
    }

    protected ComponentAdapter prepRES_dependenciesAreResolved(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        picoContainer.addComponent(PersonBean.class);
        return new SetterInjection.SetterInjector(PurseBean.class, PurseBean.class, new NullComponentMonitor(), "set", false, "", false, 
                new MethodParameters[] {
            		new BeanParameters("name", Parameter.DEFAULT),
         	}
       );
    }

    public static class WealthyPerson
            extends PersonBean {
        PurseBean purse;

        public PurseBean getPurse() {
            return purse;
        }

        public void setPurse(PurseBean purse) {
            this.purse = purse;
        }
    }

    public static class Tycoon extends PersonBean {
        String bankName;

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }
    }

    protected ComponentAdapter prepRES_failingVerificationWithCyclicDependencyException(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        picoContainer.addComponent(PersonBean.class, WealthyPerson.class);
        SetterInjection.SetterInjector componentAdapter = new SetterInjection.SetterInjector(
                PurseBean.class, PurseBean.class, new NullComponentMonitor(), "set", false, "", false,
                new MethodParameters[] {
            		new MethodParameters("setOwner", Parameter.DEFAULT),
                }
       );
        return picoContainer.as(Characteristics.NO_CACHE).addAdapter(componentAdapter).getComponentAdapter(PurseBean.class, (NameBinding) null);
    }

    @Test
    public void parentAndChildShouldReceiveSetterInjections() {
        DefaultPicoContainer picoContainer = new DefaultPicoContainer(new SetterInjection());
        picoContainer.addComponent("Pico Container");
        picoContainer.addComponent(PersonBean.class, Tycoon.class);
        SetterInjection.SetterInjector componentAdapter = new SetterInjection.SetterInjector(
                PurseBean.class, PurseBean.class, new NullComponentMonitor(), "set", false, "", false, 
                new MethodParameters[] {
            		new MethodParameters("setOwner", Parameter.DEFAULT),
                }
       );
        picoContainer.addAdapter(componentAdapter);
        
        Tycoon tycoon = picoContainer.getComponent(Tycoon.class);
        assertNotNull(tycoon);
        assertNotNull(tycoon.bankName);
        assertNotNull(tycoon.getBankName());
    }

    protected ComponentAdapter prepRES_failingInstantiationWithCyclicDependencyException(MutablePicoContainer picoContainer) {
        picoContainer.addComponent("Pico Container");
        picoContainer.addComponent(PersonBean.class, WealthyPerson.class);
        SetterInjection.SetterInjector componentAdapter = new SetterInjection.SetterInjector(
                PurseBean.class, PurseBean.class, new NullComponentMonitor(), "set", false, "", false, 
                new MethodParameters[] {
            		new MethodParameters("setOwner", Parameter.DEFAULT),
                }
                
       );
        return picoContainer.as(Characteristics.NO_CACHE).addAdapter(componentAdapter).getComponentAdapter(PurseBean.class, (NameBinding) null);
    }

    public static class A {
        private B b;
        private String string;
        private List list;

        public void setB(B b) {
            this.b = b;
        }

        public B getB() {
            return b;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public List getList() {
            return list;
        }

        public void setList(List list) {
            this.list = list;
        }
    }

    public static class A2 {
        private B b;
        private String string;
        private List list;

        public void injectB(B b) {
            this.b = b;
        }

        public B getB() {
            return b;
        }

        public String getString() {
            return string;
        }

        public void injectString(String string) {
            this.string = string;
        }

        public List getList() {
            return list;
        }

        public void injectList(List list) {
            this.list = list;
        }
    }


    public static class B {
    }

    @Test public void testAllUnsatisfiableDependenciesAreSignalled() {
        SetterInjection.SetterInjector<A> aAdapter = new SetterInjection.SetterInjector<A>("a", A.class, new NullComponentMonitor(), "set", false, "", false, null
       );
        SetterInjection.SetterInjector<B> bAdapter = new SetterInjection.SetterInjector<B>("b", B.class, new NullComponentMonitor(), "set", false, "", false, null
       );

        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.setName("parent");
        pico.addAdapter(bAdapter)
         	.addAdapter(aAdapter);

        try {
            aAdapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            assertNotNull(e.getMessage());
            String message = e.getMessage().replace("org.picocontainer.injectors.SetterInjectorTestCase$", "");
            message.trim();
            
            System.out.println("-----------  Message -----------------");
            System.out.println(message);
            System.out.println("-----------==========-----------------");
            
            //Order can't be determined so we "standardize" the order of things so to speak.
            message = message.replaceAll(" ","");
            message = message.replaceAll("\t","");
            message = message.replace("interfacejava.util.List,classjava.lang.String",
            		                   "classjava.lang.String,interfacejava.util.List");            
            assertFalse(message.contains(" "));            
            message = message.replace(
            		                  "publicvoidA.setList(java.util.List),publicvoidA.setString(java.lang.String)",
            		                  "publicvoidA.setString(java.lang.String),publicvoidA.setList(java.util.List)"
            		
            						);
            
            
            assertEquals("Got " + message, "Ahasunsatisfieddependencies[classjava.lang.String,interfacejava.util.List]formembers[publicvoidA.setString(java.lang.String),publicvoidA.setList(java.util.List)]fromparent:2<|",
                    message);
        }
    }

    @Test public void testAllUnsatisfiableDependenciesAreSignalled2() {
        SetterInjection.SetterInjector<A2> aAdapter = new SetterInjection.SetterInjector<A2>(A2.class, A2.class, new NullComponentMonitor(), "set", false, "", false);
        SetterInjection.SetterInjector<B> bAdapter = new SetterInjection.SetterInjector<B>("b", B.class, new NullComponentMonitor(), "set", false, "", false);

        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(List.class, ArrayList.class)
            .addComponent(String.class, "foo")
            .addAdapter(bAdapter)
            .addAdapter(aAdapter);

        aAdapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);

        assertNotNull(aAdapter);

        A2 a = pico.getComponent(A2.class);
        assertTrue(a.getList() == null);
        assertTrue(a.getString() == null);
    }

    public static class InitBurp {

        private Wind wind;

        public void initWind(Wind wind) {
            this.wind = wind;
        }
    }

    public static class SetterBurp {

        private Wind wind;

        public void setWind(Wind wind) {
            this.wind = wind;
        }
    }

    public static class Wind {

    }

    @Test public void testSetterMethodInjectionToContrastWithThatBelow() {

        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new SetterInjection.SetterInjector(SetterBurp.class, SetterBurp.class, new NullComponentMonitor(), "set", false, "", false, null
        ));
        pico.addComponent(Wind.class, new Wind());
        SetterBurp burp = pico.getComponent(SetterBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }

    @Test public void testNonSetterMethodInjection() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new SetterInjection.SetterInjector(InitBurp.class, InitBurp.class, new NullComponentMonitor(), "set", false, "", false, null) {
            protected String getInjectorPrefix() {
                return "init";
            }
        });
        pico.addComponent(Wind.class, new Wind());
        InitBurp burp = pico.getComponent(InitBurp.class);
        assertNotNull(burp);
        assertNotNull(burp.wind);
    }

    @Test public void testNonSetterMethodInjectionWithoutOverridingSetterPrefix() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new SetterInjection.SetterInjector(InitBurp.class, InitBurp.class, new NullComponentMonitor(), "set", false, "", false, new MethodParameters[0]
        ));
        pico.addComponent(Wind.class, new Wind());
        InitBurp burp = pico.getComponent(InitBurp.class);
        assertNotNull(burp);
        assertTrue(burp.wind == null);
    }

    @Test
    public void shouldProvideEmptyArgumentListForDefaultConstructor() throws Exception {
        final Mockery mockery = new Mockery();
        final ComponentMonitor componentMonitor = mockery.mock(ComponentMonitor.class);
        final MutablePicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(),
             new NullLifecycleStrategy(), componentMonitor, new SetterInjection());

        mockery.checking(new Expectations() {{
            oneOf(componentMonitor).newInjector(with(any(org.picocontainer.Injector.class)));
            will(returnSameInjector());
        }});

        pico.addComponent(B.class);

        mockery.checking(new Expectations() {{
            oneOf(componentMonitor).instantiating(
                    with(same(pico)), with(any(ComponentAdapter.class)), with(equal(B.class.getConstructor())));
            will(new CustomAction("return same constructor") {
                public Object invoke(Invocation invocation) {
                    return invocation.getParameter(2);
                }
            });
            oneOf(componentMonitor).instantiated(
                    with(same(pico)), with(any(ComponentAdapter.class)), with(equal(B.class.getConstructor())),
                    with(any(Object.class)), with(equal(new Object[0])), with(any(long.class))
           );
        }});
        pico.getComponent(B.class);

        mockery.assertIsSatisfied();
    }

    private CustomAction returnSameInjector() {
        return new CustomAction("return same injector") {
            public Object invoke(Invocation invocation) {
                return invocation.getParameter(0);
            }
        };
    }

    public static class C {
        private B b;
        private List l;
        private final boolean asBean;

        public C() {
            asBean = true;
        }

        public C(B b) {
            this.l = new ArrayList();
            this.b = b;
            asBean = false;
        }

        public void setB(B b) {
            this.b = b;
        }

        public B getB() {
            return b;
        }

        public void setList(List l) {
            this.l = l;
        }

        public List getList() {
            return l;
        }

        public boolean instantiatedAsBean() {
            return asBean;
        }
    }

    @Test public void testHybridBeans() {
        SetterInjection.SetterInjector bAdapter = new SetterInjection.SetterInjector("b", B.class, new NullComponentMonitor(), "set", false, "", false);
        SetterInjection.SetterInjector cAdapter = new SetterInjection.SetterInjector("c", C.class, new NullComponentMonitor(), "set", false, "", false);
        SetterInjection.SetterInjector cNullAdapter = new SetterInjection.SetterInjector("c0", C.class, new NullComponentMonitor(), "set", false, "", false);

        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(bAdapter);
        pico.addAdapter(cAdapter);
        pico.addAdapter(cNullAdapter);
        pico.addComponent(ArrayList.class);

        C c = (C) cAdapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        assertTrue(c.instantiatedAsBean());
        C c0 = (C) cNullAdapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        assertTrue(c0.instantiatedAsBean());
    }

    public static class Yin {
        private Yang yang;

        public void setYin(Yang yang) {
            this.yang = yang;
        }

        public Yang getYang() {
            return yang;
        }
    }

    public static class Yang {
        private Yin yin;

        public void setYang(Yin yin) {
            this.yin = yin;
        }

        public Yin getYin() {
            return yin;
        }
    }

    @Ignore
    @Test  //http://jira.codehaus.org/browse/PICO-188
    public void shouldBeAbleToHandleMutualDependenciesWithSetterInjection() {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching().wrap(new SetterInjection()));

        pico.addComponent(Yin.class);
        pico.addComponent(Yang.class);

        Yin yin = pico.getComponent(Yin.class);
        Yang yang = pico.getComponent(Yang.class);

        assertSame(yin, yang.getYin());
        assertSame(yang, yin.getYang());
    }
    
    public interface TestBase {
    	
    }
    
    public static class Derived1 implements TestBase {
    	
    }
    
    public static class Derived2 implements TestBase {
    	
    }
    
    public static class CompositionTest {
    	private TestBase avalue;
    	
    	private TestBase avalue2;
    	
    	public void setAvalue(TestBase newValue) {
    		avalue = newValue;
    	}
    	
    	public TestBase getAvalue() {
    		return avalue;
    	}

		public TestBase getAvalue2() {
			return avalue2;
		}

		public void setAvalue2(TestBase avalue2) {
			this.avalue2 = avalue2;
		}

    }

    
    
    @Test
    public void testComponentParemtersWithJDK7OrderParameters() {
        MutablePicoContainer localPico = new DefaultPicoContainer(new SetterInjection());

        localPico.addComponent(Derived1.class)
        	.addComponent(Derived2.class)
        	.addComponent(CompositionTest.class, CompositionTest.class,null, null,
        			new MethodParameters[] {
        				new BeanParameters("avalue", new ComponentParameter(Derived1.class)),
        				new BeanParameters("avalue2", new ComponentParameter(Derived2.class))
        			});
        
        
        CompositionTest value = localPico.getComponent(CompositionTest.class);
        assertNotNull(value);
        assertNotNull(value.getAvalue());
        assertNotNull(value.getAvalue2());
        assertTrue("Got " + value.getAvalue(), value.getAvalue().getClass().equals(Derived1.class));    	
        assertTrue("Got " + value.getAvalue2(), value.getAvalue2().getClass().equals(Derived2.class));    	
    }
    
    @Test
    public void testComponentParmetersWithReverseOrderComponentParameters() {
        MutablePicoContainer anotherLocalPico = new DefaultPicoContainer(new SetterInjection());

        anotherLocalPico.addComponent(Derived1.class)
        	.addComponent(Derived2.class)
        	.addComponent(CompositionTest.class, CompositionTest.class,
        			null, null,
        			new MethodParameters[] {
        					new BeanParameters("avalue2", new ComponentParameter(Derived2.class)),
            				new BeanParameters("avalue", new ComponentParameter(Derived1.class))
            			});
        			
        CompositionTest value = anotherLocalPico.getComponent(CompositionTest.class);
        assertNotNull(value);
        assertNotNull(value.getAvalue());
        assertNotNull(value.getAvalue2());
        assertTrue("Got " + value.getAvalue(), value.getAvalue().getClass().equals(Derived1.class));    	
        assertTrue("Got " + value.getAvalue2(), value.getAvalue2().getClass().equals(Derived2.class));    	
    }

    
    
    public static class ConstantParameterTest {
    	
    	private int aValue;

		public int getAValue() {
			return aValue;
		}

		public void setAValue(int aValue) {
			this.aValue = aValue;
		}
    	

    	
    }
    
    @Test
    public void testConstantParameters() {
        MutablePicoContainer pico = new DefaultPicoContainer(new SetterInjection());
        pico.addComponent(ConstantParameterTest.class, ConstantParameterTest.class,
        		null,
        		null,
        		new MethodParameters[] {	
        			new MethodParameters("setAValue",new ConstantParameter(3))
        		});
        
        ConstantParameterTest test = pico.getComponent(ConstantParameterTest.class);
        assertNotNull(test);
        assertEquals(3, test.getAValue());
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}

















