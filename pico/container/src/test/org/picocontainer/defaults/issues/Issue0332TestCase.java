package org.picocontainer.defaults.issues;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.parameters.ComponentParameter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class Issue0332TestCase {

    /**
     * Sample class that demonstrates literal collection handling.
     */
    public static class Searcher {
    	private final List<String> searchPath;

    	public Searcher(List<String> searchPath) {
    		this.searchPath = searchPath;
    	}

    	public List<String> getSearchPath() {
    		return searchPath;
    	}
    }
    

    /**
     * TODO Revisit this for Pico 3.
     */
    @Ignore
    @Test
    public void canInstantiateAutowiredCollectionThatAreDefinedImplicitly() {
    	MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
    	List<String> searchPath = new ArrayList<String>();
    	searchPath.add("a");
    	searchPath.add("b");

    	List<Integer> conflictingList = new ArrayList<Integer>();
    	conflictingList.add(1);
    	conflictingList.add(2);
    	pico.addComponent("conflict", conflictingList);
    	
    	pico.addComponent("searchPath",searchPath)
    		.addComponent(Searcher.class);

    	assertNotNull(pico.getComponent(Searcher.class));
    	assertNotNull(pico.getComponent(Searcher.class).getSearchPath());
    }

    @Test 
    public void canInstantiateExplicitCollectionWithComponentParameter() {
    	MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
    	List<String> searchPath = new ArrayList<String>();
    	searchPath.add("a");
    	searchPath.add("b");

    	pico.addComponent("searchPath",searchPath);
    	pico.addComponent(Searcher.class, Searcher.class, new ComponentParameter("searchPath"));

    	assertNotNull(pico.getComponent(Searcher.class));
    	assertNotNull(pico.getComponent(Searcher.class).getSearchPath());
    }

    @SuppressWarnings("serial")
	public static class StringArrayList extends ArrayList<String> {
    }

    @Test
    public void canInstantiateAutowiredCollectionThatAreDefinedExplicitlyAnotherWay() {
    	MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
    	List<String> searchPath = new StringArrayList();
    	searchPath.add("a");
    	searchPath.add("b");

    	pico.addComponent(searchPath)
    		.addComponent(Searcher.class);

    	assertNotNull(pico.getComponent(Searcher.class));
        List<String> list = pico.getComponent(Searcher.class).getSearchPath();
        assertNotNull(list);
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }



}
