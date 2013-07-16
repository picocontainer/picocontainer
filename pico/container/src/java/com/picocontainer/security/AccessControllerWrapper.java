package com.picocontainer.security;

import java.security.AccessControlException;
import java.security.Permission;

public interface AccessControllerWrapper {

	void checkPermission(Permission checkingPermission) throws AccessControlException;
}
