package org.picocontainer.web.sample.ajaxemail;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

@RunWith(JMock.class)
public class InboxTest {

	private Mockery mockery = new Mockery();
	private Collection<Message> data;
	private Persister persister;
	private User fred = new User("Fred", "password");
	private Query query;

	@Before
	public void setUp() {
		persister = mockery.mock(Persister.class);
		query = mockery.mock(Query.class);
		data = new ArrayList<Message>();
		mockery.checking(new Expectations() {
			{
				one(persister).newQuery(Message.class, "to == user_name");
				will(returnValue(query));
				one(query).declareImports("import java.lang.String");
				one(query).declareParameters("String user_name");
				one(query).execute("Fred");
				will(returnValue(data));
			}
		});
	}

	@Test
	public void testInboxCallsRightStoreMethod() {
		Inbox inbox = new Inbox(persister, fred, new QueryStore());
		assertEquals(0, inbox.messages().length);
	}

}