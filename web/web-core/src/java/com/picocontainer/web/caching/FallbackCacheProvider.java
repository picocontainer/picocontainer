package com.picocontainer.web.caching;

import javax.cache.CacheException;
import java.util.Map;
import java.util.HashMap;

import com.picocontainer.web.caching.JCacheProvider;
import com.picocontainer.web.Cache;

public class FallbackCacheProvider extends JCacheProvider {

    final Map fallBackImpl = new HashMap();

    @Override
    public Cache provide() throws CacheException {
        try {
            return super.provide();
        } catch (NullPointerException e) {
            return new Cache() {
                public Object get(Object key) {
                    return fallBackImpl.get(key);
                }

                public void put(Object key, Object toCache) {
                    fallBackImpl.put(key, toCache);
                }
            };
        }
    }

}
