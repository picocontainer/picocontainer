package org.picocontainer.web;

public interface Cache {
    Object get(Object key);
    void put(Object key, Object toCache);
}
