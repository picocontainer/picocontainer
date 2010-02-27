package org.picocontainer.script.testmodel;

import java.util.Map;

public final class MapSupport
{
    private final Map<String, Entity> aMapOfEntities;

    public MapSupport(Map<String, Entity> aMapOfEntities)
    {
        this.aMapOfEntities = aMapOfEntities;
    }

    public Map<String, Entity> getAMapOfEntities()
    {
        return aMapOfEntities;
    }
}
