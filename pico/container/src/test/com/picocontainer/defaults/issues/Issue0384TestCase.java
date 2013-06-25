package com.picocontainer.defaults.issues;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.containers.CompositePicoContainer;

public class Issue0384TestCase {

  public static interface AnInterface {

  }
  public static interface AnInterface2 {

  }

  public static class Impl1 implements AnInterface {

  }

  public static class Impl2 implements AnInterface {

  }

  @Test
  public void testComposition() throws Exception {
    DefaultPicoContainer primary = new DefaultPicoContainer();
    primary.addComponent(AnInterface.class, Impl1.class);

    DefaultPicoContainer secondary = new DefaultPicoContainer();
    secondary.addComponent(AnInterface.class, Impl2.class);

    CompositePicoContainer composite = new CompositePicoContainer(primary, secondary);
    DefaultPicoContainer child = new DefaultPicoContainer(composite);

    assertEquals(Impl1.class, child.getComponent(Impl1.class).getClass());
    assertEquals(Impl2.class, child.getComponent(Impl2.class).getClass());
    assertEquals(Impl1.class, child.getComponent(AnInterface.class).getClass());
  }

}