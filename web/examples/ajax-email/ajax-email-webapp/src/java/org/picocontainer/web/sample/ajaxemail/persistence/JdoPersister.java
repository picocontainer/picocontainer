package org.picocontainer.web.sample.ajaxemail.persistence;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.picocontainer.web.sample.ajaxemail.Query;

public class JdoPersister implements Persister {

    PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("transactional").getPersistenceManager();

    public void makePersistent(Object persistent) {
        pm.makePersistent(persistent);
    }

    public void beginTransaction() {
        pm.currentTransaction().begin();
    }

    public void commitTransaction() {
        pm.currentTransaction().commit();        
    }

    public Query newQuery(Class<?> type, String query) {
        final javax.jdo.Query jdoQuery = pm.newQuery(type, query);
        return new Query() {
            public Object execute(Object arg) {
                if (arg == null) {
                    return jdoQuery.execute();                    
                } else {
                    return jdoQuery.execute(arg);
                }

            }

            public void declareImports(String imports) {
                jdoQuery.declareImports(imports);
            }

            public void declareParameters(String parameters) {
                jdoQuery.declareParameters(parameters);
            }

        };
    }

    public void deletePersistent(Object persistent) {
        pm.deletePersistent(persistent);
    }

}
