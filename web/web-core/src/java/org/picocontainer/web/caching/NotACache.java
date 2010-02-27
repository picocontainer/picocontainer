package org.picocontainer.web.caching;

import org.picocontainer.web.Cache;

class NotACache implements Cache {

    public Object get(Object key) {
        return null;
    }

    public void put(Object key, Object toCache) {
    }
}
