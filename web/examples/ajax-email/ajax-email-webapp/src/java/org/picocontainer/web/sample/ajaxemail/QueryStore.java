package org.picocontainer.web.sample.ajaxemail;

import java.util.Map;
import java.util.HashMap;

/**
 * Simple store for JDO Queries, to cache at app level and prevent bytecode regeneration for
 * each query
 */
public class QueryStore {

    private transient Map<String,Query> queries = new HashMap<String,Query>();

    public Query get(String key) {
        return queries.get(key);
    }

    public void put(String key, Query query) {
        queries.put(key, query);
    }
}
