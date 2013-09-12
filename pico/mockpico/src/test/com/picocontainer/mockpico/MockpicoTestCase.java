package com.picocontainer.mockpico;
/**
 * Copyright (c) 2010 ThoughtWorks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.injectors.AnnotatedFieldInjection;
import com.picocontainer.injectors.AnnotatedMethodInjection;
import com.picocontainer.injectors.NamedMethodInjection;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.picocontainer.injectors.Injectors.CDI;
import static com.picocontainer.injectors.Injectors.SDI;
import static com.picocontainer.mockpico.Mockpico.makePicoContainer;
import static com.picocontainer.mockpico.Mockpico.mockDepsFor;
import static com.picocontainer.mockpico.Mockpico.resetAll;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


public class MockpicoTestCase {

    private static C c = new C();
    private static D d = new D();
    private static B b = new B(c);

    @Test
    public void canSpecifyCustomInjectionTypes() {

        A a = mockDepsFor(A.class)
                .using(makePicoContainer(CDI(), SDI()))
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(mockCandB()),
                setterCalledWith(mockD())
        ).to(a);
    }

    @Test
    public void canUseCustomMockMaker() {

        final StringBuilder sb = new StringBuilder();

        Mockpico.Mocker mocker = new Mockpico.Mocker() {
            public <T> T mock(Class<T> classToMock) {
                sb.append("Mocking:" + classToMock.getSimpleName() + "\n");
                return Mockito.mock(classToMock);
            }
        };
        mockDepsFor(A.class)
                .using(makePicoContainer(CDI(), SDI()))
                .make(mocker);

        assertThat(sb.toString(), is(equalTo(
                "Mocking:C\n" +
                "Mocking:B\n" +
                "Mocking:D\n")));
    }

    @Test
    public void canLeverageRealInjecteesForCustomInjectionTypes() {

        A a = mockDepsFor(A.class)
                .using(makePicoContainer(CDI(), SDI()))
                .withInjectees(b, c, d) // b, c and d are real
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(memberVarsCandB()),
                setterCalledWith(memberVarD())
        ).to(a);
    }

    @Test
    public void canSpecifyTypesForPicoToMakeIntoInstancesForInjection() {

        A a = mockDepsFor(A.class)
                .using(makePicoContainer(CDI(), SDI()))
                .withInjectees(B.class, C.class, D.class)
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(newCandB()),
                setterCalledWith(newD())
        ).to(a);
    }

    @Test
    public void defaultsAreConstructorAnnotatedFieldAndMethodInjectionWithSuppliedInjectees() {

        A a = mockDepsFor(A.class)
                .withInjectees(b, c)
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(memberVarsCandB()),
                atInjectMethodCalledWith(memberVarB()),
                autowiredMethodCalledWith(memberVarB()),
                autowiredFieldSetTo(memberVarB()),
                atInjectFieldSetTo(memberVarB())
        ).to(a);
    }

    @Test
    public void settersCanBeAddedToDefaultInjectionTypes() {

        A a = mockDepsFor(A.class)
                .withSetters()
                .withInjectees(b, c, d)
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(memberVarsCandB()),
                atInjectMethodCalledWith(memberVarB()),
                autowiredMethodCalledWith(memberVarB()),
                setterCalledWith(memberVarD()),
                autowiredFieldSetTo(memberVarB()),
                atInjectFieldSetTo(memberVarB())
        ).to(a);
    }

    @Test
    public void canUseAPicoContainerHandedInAndJournalInjectionsToSpecialObject() {
        MutablePicoContainer pico = makePicoContainer(CDI(), SDI(),
                new AnnotatedFieldInjection(Inject.class, Mockpico.JSR330_ATINJECT, Mockpico.SPRING_AUTOWIRED));

        Journal journal = new Journal();
        A a = mockDepsFor(A.class)
                .using(pico)
                .journalTo(journal)
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(mockCandB()),
                setterCalledWith(mockD()),
                autowiredFieldSetTo(mockB()),
                atInjectFieldSetTo(mockB())
                
        ).to(a);


        String journalString = replaceNumbericObjectIDsSoThatStringComparisonCanWork(journal);

        assertEquals("Constructor being injected:\n" +
                "  arg[0] type:class com.picocontainer.mockpico.MockpicoTestCase$C, with: Mock for C, hashCode: <HC#0>\n" +
                "  arg[1] type:class com.picocontainer.mockpico.MockpicoTestCase$B, with: Mock for B, hashCode: <HC#1>\n" +
                "Method 'setIt' being injected: \n" +
                "  arg[0] type:class com.picocontainer.mockpico.MockpicoTestCase$D, with: Mock for D, hashCode: <HC#2>\n" +
                "Field being injected: 'b1' with: Mock for B, hashCode: <HC#1>\n" +
                "Field being injected: 'b2' with: Mock for B, hashCode: <HC#1>\n", journalString);
    }

    @Test
    public void defaultsAreConstructorAnnotatedFieldAndMethodInjectionAndMockitoSuppliesInjectees() {

        A a = mockDepsFor(A.class).make();

        assertTheseHappenedInOrder(
                aMadeWith(mockCandB()),
                atInjectMethodCalledWith(mockB()),
                autowiredMethodCalledWith(mockB()),
                autowiredFieldSetTo(mockB()),
                atInjectFieldSetTo(mockB())
        ).to(a);
    }

    @Test
    public void mockitoMocksCanBePassedIn() {
        List list1 = mock(List.class);

        NeedsList nl = mockDepsFor(NeedsList.class)
                .withInjectees(list1)
                .make();

        assertSame(list1, nl.list);
    }

    @Test
    public void canMockPrimivitesAndAlsoUseCustomAnnotationInjectionType() {

        Journal journal = new Journal();
        A a = mockDepsFor(A.class)
                .using(makePicoContainer(CDI(), new AnnotatedMethodInjection(false, A.Foobarred.class)))
                .journalTo(journal)
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(mockCandB()),
                atInjectMethodCalledWith(mockB()),
                customAnnotatedMethodCalledWith(aBunchOfPrimitives())
        ).to(a);

        String journalString = replaceNumbericObjectIDsSoThatStringComparisonCanWork(journal);

        assertThat(journalString, equalTo("Constructor being injected:\n" +
                "  arg[0] type:class com.picocontainer.mockpico.MockpicoTestCase$C, with: Mock for C, hashCode: <HC#0>\n" +
                "  arg[1] type:class com.picocontainer.mockpico.MockpicoTestCase$B, with: Mock for B, hashCode: <HC#1>\n" +
                "Method 'inj3ct' being injected: \n" +
                "  arg[0] type:class com.picocontainer.mockpico.MockpicoTestCase$B, with: Mock for B, hashCode: <HC#1>\n" +
                "Method 'foobar' being injected: \n" +
                "  arg[0] type:class java.lang.String, with: \n" +
                "  arg[1] type:int, with: 0\n" +
                "  arg[2] type:double, with: 0.0\n" +
                "  arg[3] type:class java.lang.Double, with: 0.0\n" +
                "  arg[4] type:boolean, with: false\n" +
                "  arg[5] type:float, with: 0.0\n" +
                "  arg[6] type:byte, with: 0\n" +
                "  arg[7] type:short, with: 0\n" +
                "  arg[8] type:class java.math.BigInteger, with: Mock for BigInteger, hashCode: <HC#3>\n" +
                "  arg[9] type:char, with: \u0000\n" +
                "  arg[10] type:class java.lang.Long, with: 0\n"));
    }

    @Test
    public void canMockGenericThing() {

        Journal journal = new Journal();
        A a = mockDepsFor(A.class)
                .using(makePicoContainer(CDI(), new NamedMethodInjection("shove")))
                .journalTo(journal)
                .make();

        assertTheseHappenedInOrder(
                aMadeWith(mockCandB()),
                shoveItCalledWithEmptyListOfDs()
        ).to(a);

        String journalString = replaceNumbericObjectIDsSoThatStringComparisonCanWork(journal);

        assertThat(journalString, equalTo("Constructor being injected:\n" +
                "  arg[0] type:class com.picocontainer.mockpico.MockpicoTestCase$C, with: Mock for C, hashCode: <HC#0>\n" +
                "  arg[1] type:class com.picocontainer.mockpico.MockpicoTestCase$B, with: Mock for B, hashCode: <HC#1>\n" +
                "Method 'shoveIt' being injected: \n" +
                "  arg[0] type:interface java.util.List, with: Mock for List, hashCode: <HC#2>\n"));
    }

    private String replaceNumbericObjectIDsSoThatStringComparisonCanWork(Journal journal) {
        Pattern eightOrMoreDigits = Pattern.compile("\\d{8,}");
        List<String> hashes = new ArrayList<String>();
        Matcher matcher = eightOrMoreDigits.matcher(journal.toString());
        while (matcher.find()) {
            hashes.add(matcher.group());
        }
        String retVal = journal.toString();
        for (int i = 0; i < hashes.size(); i++) {
            retVal = retVal.replace(hashes.get(i), "<HC#" + i + ">");
        }
        return retVal;
    }

    @Test
    public void mocksUsedCanReceiveVerifyNoMoreInteractions() {
        MutablePicoContainer mocks = makePicoContainer();

        NeedsList nl = mockDepsFor(NeedsList.class)
                .using(mocks)
                .make();

        nl.oops();
        try {
            Mockpico.verifyNoMoreInteractionsForAll(mocks);
            fail("should have barfed");
        } catch (NoInteractionsWanted e) {
            // expected  
        }
    }

    @Test
    public void mocksUsedCanReceiveReset() {
        MutablePicoContainer mocks = makePicoContainer();

        NeedsList nl = mockDepsFor(NeedsList.class)
                .using(mocks)
                .make();

        nl.oops();
        resetAll(mocks);
        Mockito.verifyNoMoreInteractions(mocks.getComponent(List.class));
    }

    @Test
    public void nonsenseAnnotationDoesntUpsetMockPico() {
        Class<? extends Annotation> ann = Mockpico.getInjectionAnnotation("foo.Bar");
        assertThat(ann.getName(), equalTo("com.picocontainer.mockpico.Mockpico$AnnotationNotFound"));
    }

    public static class NeedsList {
        private List list;

        public NeedsList(List list) {
            this.list = list;
        }

        public void oops() {
            list.add("oops");
        }

    }

    public static class A {

        private StringBuilder sb = new StringBuilder();
        private Map<Object, String> printed = new HashMap<Object, String>();
        private int mocks;
        private int reals;

        @Autowired
        private B b1;

        @Inject
        private B b2;

        @Override
        public String toString() {
            String s = sb.toString();
            if (b1 != null) {
                s = s + ",b1=" + prt(b1);
            }
            if (b2 != null) {
                s = s + ",b2=" + prt(b2);
            }
            return s;
        }

        private String prt(List<D> objs) {
            String p = "*empty*";
            for (D d1 : objs) {
                p = p + ", " + prt(d1);
            }
            return "Ds[" + p.replace("*empty*,", "*empty*") + "]";
        }

        private String prt(Object obj) {
            String p = printed.get(obj);
            if (p == null) {
                if (obj.toString().indexOf("Mock for") > -1) {
                    Class<?> parent = obj.getClass().getSuperclass();
                    if (parent == Object.class) {
                        parent = obj.getClass().getInterfaces()[0];
                    }
                    p = "mock["+ parent.getName().substring(parent.getName().lastIndexOf('.')+1).replace("MockpicoTestCase$", "") +"]#" + mocks++;
                } else {
                    String name = obj.getClass().getName();
                    if (obj == b) {
                        return "b<c>";
                    } else if (obj == c) {
                        return "c";
                    } else if (obj == d) {
                        return "d";
                    }
                    p = name.substring(name.lastIndexOf(".")+1).replace("MockpicoTestCase$", "") + "#" + reals++;
                    if (obj instanceof B) {
                        p = p +  "<" + prt(((B) obj).c) + ">";
                    }
                }
                printed.put(obj, p);
            }
            return p;
        }


        public A(C c, B b) {
            sb.append("A(" + prt(c) + "," + prt(b) + ")");
        }

        public void setIt(D d) {
            sb.append(",setIt(" + prt(d) + ")");
        }

        public void shoveIt(List<D> ds) {
            sb.append(",shoveIt(" + prt(ds) + ")");
        }

        @Inject
        public void inj3ct(B b) {
            sb.append(",inj3ct(" + prt(b) + ")");
        }

        @Autowired
        public void aut0wireMe(B b) {
            sb.append(",aut0wireMe(" + prt(b) + ")");
        }

        @Foobarred
        public void foobar(String str, int iint,
                           double dbl,
                           Double dbl2,
                           boolean bool,
                           float flt,
                           byte byt,
                           short shrt,
                           BigInteger bigInt,
                           char chr,
                           Long lng) {

            sb.append(",foobar(" + prt(str) + "," +
                    prt(iint) + "," +
                    prt(dbl) + "," +
                    prt(dbl2) + "," +
                    prt(flt) + "," +
                    prt(byt) + "," +
                    prt(shrt) + "," +
                    prt(bigInt) + "," +
                    prt(chr) + "," +
                    prt(lng) +
                    ")");
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
        public static @interface Foobarred {
        }
    }

    public static class C {
    }

    public static class D {
    }

    public static class B {
        private final C c;

        public B(C c) {
            this.c = c;
        }
    }

    private String newD() {
        return "D#2";
    }

    private String newCandB() {
        return "C#0,B#1<C#0>";
    }

    private String mockD() {
        return "mock[D]#2";
    }

    private String setterCalledWith(String with) {
        return ",setIt(" + with + ")";
    }

    private String memberVarD() {
        return "d";
    }

    private String autowiredMethodCalledWith(String with) {
        return ",aut0wireMe(" + with + ")";
    }

    private String autowiredFieldSetTo(String to) {
        return ",b1=" + to;
    }

    private String atInjectFieldSetTo(String to) {
        return ",b2=" + to;
    }

    private String memberVarB() {
        return "b<c>";
    }

    private String memberVarsCandB() {
        return "c,b<c>";
    }

    private String atInjectMethodCalledWith(String with) {
        return ",inj3ct("+with+")";
    }

    private String shoveItCalledWithEmptyListOfDs() {
        return ",shoveIt(Ds[*empty*])";
    }

    private String aMadeWith(String with) {
        return "A("+with+")";
    }

    private String mockB() {
        return "mock[B]#1";
    }

    private String mockCandB() {
        return "mock[C]#0,mock[B]#1";
    }

    private String aBunchOfPrimitives() {
        return "String#0,Integer#1,Double#2,Double#2,Float#3,Byte#4,Short#5,mock[BigInteger]#2,Character#6,Long#7";
    }

    private String customAnnotatedMethodCalledWith(String with) {
        return ",foobar("+with+")";
    }

    private Foo assertTheseHappenedInOrder(String... things) {
        return new Foo(things);
    }

    private static class Foo {
        private  String whatShouldHaveHappened = "";

        public Foo(String[] happenings) {
            for (String happening : happenings) {
                whatShouldHaveHappened += happening;
            }
        }

        public void to(A a) {
            String whatActuallyHappened = a.toString();
            assertEquals(whatShouldHaveHappened, whatActuallyHappened);
        }
    }

}
