package org.picocontainer.web.sample.ajaxemail;

import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

/**
 * Inbox is a type of Mailbox for a user.
 */
public class Inbox extends Mailbox {

    public Inbox(Persister persister, User user, QueryStore queryStore) {
        super(persister, user, queryStore);
    }

    protected void checkUser(Message message) {
        if (!message.getTo().equals(getUserName())) {
            throwNotForThisUser();
        }
    }

    protected String fromOrTo() {
        return "to";
    }

}
