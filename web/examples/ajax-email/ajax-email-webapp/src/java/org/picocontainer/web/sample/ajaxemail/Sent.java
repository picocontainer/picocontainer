package org.picocontainer.web.sample.ajaxemail;

import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

/**
 * Send is a type of Mailbox for a user with a special function for sending
 */
public class Sent extends Mailbox {

    public Sent(Persister persister, User user, QueryStore queryStore) {
        super(persister, user, queryStore);
    }

    public Message send(String to, String subject, String message) {
        return super.addMessage(new Message(getUserName(), to, subject,
                message, false, System.currentTimeMillis()));
    }

    /**
     * A guard to prevent someone trying to read emails for others users (they are not logged in as)
     * @param message
     */
    protected void checkUser(Message message) {
        if (message.getTo().equals(getUserName())) {
            throwNotForThisUser();
        }
    }

    protected String fromOrTo() {
        return "from";
    }


}
