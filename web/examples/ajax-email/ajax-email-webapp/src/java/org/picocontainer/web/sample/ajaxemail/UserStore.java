package org.picocontainer.web.sample.ajaxemail;

import java.util.Collection;

import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

public class UserStore {

    private transient Persister persister;
    private transient final QueryStore queryStore;

    public UserStore(Persister persister, QueryStore queryStore) {
        this.persister = persister;
        this.queryStore = queryStore;
    }

    @SuppressWarnings("unchecked")
	public User getUser(String name) {
        Query query = queryStore.get("GU");
        if (query == null) {
            query = persister.newQuery(User.class, "name == user_name");
            query.declareImports("import java.lang.String");
            query.declareParameters("String user_name");
            queryStore.put("GU", query);
        }
        Collection<User> users = (Collection<User>) query.execute(name);
        if (users != null && users.size() > 0) {
            return users.iterator().next();
        } else {
            return null;
        }
    }
    
}
