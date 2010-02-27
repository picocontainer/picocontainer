package org.picocontainer.web.caching;

import javax.cache.*;
import javax.cache.Cache;
import java.util.Map;
import java.util.HashMap;

import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.web.*;

public class JCacheProvider extends ProviderAdapter {

    private static final Map props = new HashMap();
    static {
        // com.google.appengine.api.memcache.stdimpl.GCacheFactory.EXPIRATION_DELTA (yeesh) == 0
        // 120 == 1 min
        props.put(0, 120);        
    }

    public org.picocontainer.web.Cache provide() throws CacheException {
        CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
        final javax.cache.Cache jCache = cacheFactory.createCache(props);
        return new org.picocontainer.web.Cache() {
            public Object get(Object key) {
                return jCache.get(key);
            }
            public void put(Object key, Object toCache) {
                jCache.put(key, toCache);
            }
        };
    }
}