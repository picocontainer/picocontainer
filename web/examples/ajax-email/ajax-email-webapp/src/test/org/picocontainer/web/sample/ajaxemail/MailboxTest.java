package org.picocontainer.web.sample.ajaxemail;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

public class MailboxTest {

    private Mockery mockery = new Mockery();

    private List<Message> data;
    private User fred = new User("Fred", "password");
    private Message message = new Message("Fred", "to", "subj", "message", false, 12345);
    private Persister persister;
    private Query query;

    @Before
    public void setUp() {

        data = new ArrayList<Message>();
        data.add(message);
        message.setId(2L);
        persister = mockery.mock(Persister.class);
        query = mockery.mock(Query.class);

    }

    @Test
    public void testReadingOfMessages() {
        mockery.checking(new Expectations(){{
    		one(persister).newQuery(Message.class, "XXX == user_name");
    		will(returnValue(query));
            one(query).declareImports("import java.lang.String");
            one(query).declareParameters("String user_name");
            one(query).execute("Fred");
            will(returnValue(data));
        }});

        Mailbox mailbox = new MyMailbox(persister, fred);
        Message[] messages = mailbox.messages();
        assertEquals(1, messages.length);
        assertEquals(message.getId(), messages[0].getId());
        verifyMessage(messages[0], false, null);
    }

    @Test
    public void testReadOfSingleMessageFlipsReadFlag() {
        mockery.checking(new Expectations(){{
            one(persister).newQuery(Message.class, "id == message_id");
    		will(returnValue(query));
            one(persister).beginTransaction();
            one(persister).commitTransaction();
            one(query).declareImports("import java.lang.Long");
            one(query).declareParameters("Long message_id");                        
            one(query).execute(2L);
            will(returnValue(data));
        }});

        Mailbox mailbox = new MyMailbox(persister, fred);
        assertEquals(message, mailbox.read(2));
        verifyMessage(message, true, "message");
    }

    @Test
    public void testReadOfMissingMessageCausesException() {
        mockery.checking(new Expectations(){{
            one(persister).newQuery(Message.class, "id == message_id");
    		will(returnValue(query));
            one(persister).beginTransaction();
            one(persister).commitTransaction();
            one(query).declareImports("import java.lang.Long");
            one(query).declareParameters("Long message_id");
            one(query).execute(22222L);
            will(returnValue(new ArrayList<Object>()));
        }});

		Mailbox mailbox = new MyMailbox(persister, fred);
        try {
            mailbox.read(22222);
            fail();
        } catch (AjaxEmailException e) {
            assertEquals("no such message ID", e.getMessage());
        }
    }

    @Test
    public void testDeleteOfSingleMessage() {
        mockery.checking(new Expectations(){{
            one(persister).newQuery(Message.class, "id == message_id");
            will(returnValue(query));
            one(query).declareImports("import java.lang.Long");
            one(query).declareParameters("Long message_id");
            one(query).execute(2L);
            will(returnValue(data));
            one(persister).deletePersistent(message);
        }});

        Mailbox mailbox = new MyMailbox(persister, fred);
        mailbox.delete(2);
    }

    @Test
    public void testDeleteOfMissingMessageCausesException() {
        mockery.checking(new Expectations(){{
            one(persister).newQuery(Message.class, "id == message_id");
            will(returnValue(query));
            one(query).declareImports("import java.lang.Long");
            one(query).declareParameters("Long message_id");
            one(query).execute(22222L);
            will(returnValue(null));
        }});
        Mailbox mailbox = new MyMailbox(persister, fred);
        try {
            mailbox.delete(22222);
        } catch (AjaxEmailException e) {
            assertEquals("no such message ID", e.getMessage());
        }
    }


    private void verifyMessage(Message m, boolean read, String messageVal) {
        assertEquals("to", m.getTo());
        assertEquals(2L, (long) m.getId());
        assertEquals("subj", m.getSubject());
        assertEquals(messageVal, m.getMessage());
        assertEquals("Fred", m.getFrom());
        assertEquals(read, m.isRead());
        assertEquals(12345, m.getSentTime().getTime());
    }

    private class MyMailbox extends Mailbox {
        public MyMailbox(Persister pm, User user) {
            super(pm, user, new QueryStore());
        }

        protected void checkUser(Message message) {
        }

        protected String fromOrTo() {
            return "XXX";
        }
    }
}