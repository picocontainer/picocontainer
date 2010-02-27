package org.picocontainer.web.sample.ajaxemail.persistence;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.picocontainer.web.sample.ajaxemail.Query;

public class JdoPersister implements Persister {

//    public static interface GoogleServices {
//        <T> T getService(Class<T> clazz);
//        <T> T getService(Class<T> clazz, String hint);
//    }
//
//    public static class FooServlet extends HttpServlet {
//
//        private final PersistenceManager pm;
//
//        /** GAE fills this in */
//        public FooServlet(GoogleServices services) {
//            this.pm = services.getService(PersistenceManager.class,
//                    "transactional");
//        }
//
//        /** But let us also stay compatible with non AppEngine servers */
//        public FooServlet() {
//            this.pm = JDOHelper.getPersistenceManagerFactory("transactional")
//            .getPersistenceManager();
//        }
//
//        protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
//        }
//    }

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
