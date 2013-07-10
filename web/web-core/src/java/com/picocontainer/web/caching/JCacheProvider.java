package com.picocontainer.web.caching;

import java.util.HashMap;
import java.util.Map;

import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import com.picocontainer.injectors.ProviderAdapter;

public class JCacheProvider extends ProviderAdapter {

    private static final Map props = new HashMap();
    static {
        // com.google.appengine.api.memcache.stdimpl.GCacheFactory.EXPIRATION_DELTA (yeesh) == 0
        // 120 == 1 min
        props.put(0, 120);        
    }

    public com.picocontainer.web.Cache provide() throws CacheException {
        CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
        final javax.cache.Cache jCache = cacheFactory.createCache(props);
        return new com.picocontainer.web.Cache() {
            public Object get(Object key) {
                return jCache.get(key);
            }
            public void put(Object key, Object toCache) {
                jCache.put(key, toCache);
            }
        };
    }
}