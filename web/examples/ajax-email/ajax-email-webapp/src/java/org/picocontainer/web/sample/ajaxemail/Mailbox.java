package org.picocontainer.web.sample.ajaxemail;

import java.util.List;
import java.util.Collection;

import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

/**
 * Abstract Mailbox
 */
public abstract class Mailbox {

    private final Persister persister;
    private final User user;
    private final QueryStore queryStore;

    public Mailbox(Persister persister, User user, QueryStore queryStore) {
        this.persister = persister;
        this.user = user;
        this.queryStore = queryStore;
    }

    protected Message addMessage(Message newMsg) {
        persister.makePersistent(newMsg);
        return newMsg;
    }

    /**
     * Read a message (flip its read flag if not already)
     * @param id the message to read
     * @return the message
     */
    public Message read(long id) {
        Message message = getMessage(id);
        if (!message.isRead()) {
            persister.beginTransaction();
            message.markRead();
            persister.commitTransaction();
        }
        return message;
    }

    @SuppressWarnings("unchecked")
	private Message getMessage(long messageId) {
        Collection<Message> coll = (Collection<Message>) getSingleMessageQuery().execute(messageId);
        if (coll != null && coll.size() == 1) {
            Message message = coll.iterator().next();
            checkUser(message);
            return message;
        }
        throw new AjaxEmailException("no such message ID");
    }

    protected abstract void checkUser(Message message) ;

    protected void throwNotForThisUser() {
        throw new AjaxEmailException("email ID not for the user logged in");
    }

    private Query getSingleMessageQuery() {
        String key = "SM_" + fromOrTo();
        Query query = queryStore.get(key);
        if (query == null) {
            query = persister.newQuery(Message.class, "id == message_id");
            query.declareImports("import java.lang.Long");
            query.declareParameters("Long message_id");
            queryStore.put(key, query);
        }
        return query;
    }

    /**
     * Delete a message
     * @param id the message to delete
     */
    public void delete(long id) {
        Message message = getMessage(id);
        persister.deletePersistent(message);
    }

    /**
     * List the messages for the user
     * @return the messages
     */
    @SuppressWarnings("unchecked")
	public Message[] messages() {
        Query query = getMultipleMessageQuery();
        List<Message> messages = (List<Message>) query.execute(user.getName());
        return cloneListWithoutMessageBody(messages);
    }

    private Message[] cloneListWithoutMessageBody(List<Message> messages) {
        Message[] array = new Message[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            array[i] = new Message(message.getFrom(), message.getTo(), message.getSubject(),
                    null, message.isRead(), message.getSentTime().getTime());
            array[i].setId(message.getId());
        }
        return array;
    }

    private Query getMultipleMessageQuery() {
        String key = "MM_" + fromOrTo();
        Query query = queryStore.get(key);
        if (query == null) {
            query = persister.newQuery(Message.class, fromOrTo() + " == user_name");
            query.declareImports("import java.lang.String");
            query.declareParameters("String user_name");
            queryStore.put(key, query);
        }
        return query;
    }

    protected abstract String fromOrTo();

    protected String getUserName() {
        return user.getName();
    }

    public String toString() {
        return getUserName();
    }

}
