package org.picocontainer.web.sample.ajaxemail.persistence;

import java.util.ArrayList;
import java.util.List;

import org.picocontainer.web.sample.ajaxemail.Message;
import org.picocontainer.web.sample.ajaxemail.Query;
import org.picocontainer.web.sample.ajaxemail.User;

public class InMemoryPersister implements Persister {

    private List<Message> messages = new ArrayList<Message>();
    private List<User> users = new ArrayList<User>();
    private long messageCounter = 0;

    public void makePersistent(Object persistent) {
        if (persistent instanceof Message) {

            Message message = (Message) persistent;
            message.setId(++messageCounter);
            messages.add(message);
        } else {
            users.add((User) persistent);
        }
    }

    public void beginTransaction() {
    }

    public void commitTransaction() {
    }

    public Query newQuery(final Class<?> type, final String query) {

        return new Query() {
            public Object execute(Object arg) {
                if (type == Message.class) {
                    List<Message> result = new ArrayList<Message>();
                    if (query.equals("id == message_id")) {
                        for (Message message : messages) {
                            if (message.getId() == (Long) arg) {
                                result.add(message);
                            }
                        }
                    } else if (query.equals("from == user_name")) {
                        for (Message message : messages) {
                            if (message.getFrom().equals((String) arg)) {
                                result.add(message);
                            }
                        }
                    } else if (query.equals("to == user_name")) {
                        for (Message message : messages) {
                            if (message.getTo().equals((String) arg)) {
                                result.add(message);
                            }
                        }
                    } else if (query.equals("id > -1")) {
                        result = messages;
                    }
                    return result;
                } else {
                    List<User> result = new ArrayList<User>();
                    for (User user : users) {
                        if (user.getName().equals((String) arg)) {
                            result.add(user);
                        }
                    }
                    return result;
                }
            }

            public void declareImports(String imports) {
            }

            public void declareParameters(String parameters) {
            }

        };
    }

    public void deletePersistent(Object persistent) {
        if (persistent instanceof Message) {
            messages.remove((Message) persistent);
        } else {
            users.remove((User) persistent);
        }
    }

}