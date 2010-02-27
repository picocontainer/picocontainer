package org.picocontainer.web.sample.ajaxemail;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class User {

    @PrimaryKey
    private String name;

    @Persistent
    private String password;

	public User(String name, String password) {
		this.name = name;
        this.password = password;
    }

	public String getName() {
		return name;
	}

    public String getPassword() {
        return password;
    }

}
