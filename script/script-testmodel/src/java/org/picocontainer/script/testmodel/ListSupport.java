package org.picocontainer.script.testmodel;

import java.util.List;

/**
 * Test for list support.
 *
 * @author Jeff Steward
 */
public final class ListSupport
{
    private final List<Entity> aListOfEntityObjects;

    public ListSupport(List<Entity> aListOfEntityObjects)
    {
        this.aListOfEntityObjects = aListOfEntityObjects;
    }

    public List<Entity> getAListOfEntityObjects()
    {
        return aListOfEntityObjects;
    }
}