package org.picocontainer.web.sample.ajaxemail.persistence;

import org.picocontainer.web.sample.ajaxemail.Query;

public interface Persister {
	
    void makePersistent(Object persistent);

    void beginTransaction();

    void commitTransaction();

    Query newQuery(Class<?> type, String query);

    void deletePersistent(Object persistent);

}
