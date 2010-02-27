
package org.picocontainer.web.sample.ajaxemail;

import java.util.Date;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Message {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String from;

    @Persistent
    private String to;

    @Persistent
    private String subject;

    @Persistent
    private String message;
    
    @Persistent
    private Date sentTime;

    @Persistent
    private boolean read;

    public Message(String from, String to,
            String subject, String message, boolean isRead, long time) {
    	this.from = from;
    	this.to = to;
    	this.subject = subject;
    	this.message = message;
    	this.read = isRead;
        this.sentTime = new Date(time);
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

    public String getTo() {
		return to;
	}

    public boolean isRead() {
		return read;
	}

    public String getSubject() {
		return subject;
	}

    public String getMessage() {
		return message;
	}

    public Date getSentTime() {
		return sentTime;
	}

    public void markRead() {
        read = true;
    }

}
