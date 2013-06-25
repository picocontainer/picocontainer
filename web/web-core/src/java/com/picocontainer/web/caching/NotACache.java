package com.picocontainer.web.caching;

import com.picocontainer.web.Cache;

class NotACache implements Cache {

    public Object get(Object key) {
        return null;
    }

    public void put(Object key, Object toCache) {
    }
}
