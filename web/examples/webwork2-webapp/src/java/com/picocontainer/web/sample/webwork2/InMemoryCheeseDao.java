/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.sample.webwork2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stephen Molitor
 */
@SuppressWarnings("serial")
public class InMemoryCheeseDao implements CheeseDao, Serializable {

    private final Map<String,Cheese> cheeses;

    public InMemoryCheeseDao() {
        cheeses = new ConcurrentHashMap<String,Cheese>();
        cheeses.put("Cheddar", new Cheese("Cheddar","England"));
        cheeses.put("Brie", new Cheese("Brie","France"));
        cheeses.put("Dolcelatte", new Cheese("Dolcelatte","Italy"));
        cheeses.put("Manchego", new Cheese("Manchego","Spain"));
    }

    public void save(Cheese cheese) {
        cheeses.put(cheese.getName(), cheese);
    }

    public void remove(Cheese cheese) {
        cheeses.remove(cheese.getName());
    }
    public Cheese get(String name) {
        return (Cheese) cheeses.get(name);
    }

    public Collection<Cheese> all() {
        return new ArrayList<Cheese>(cheeses.values());
    }

}