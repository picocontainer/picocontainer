package com.picocontainer.security;

import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Arguments for PicoAccessPermission
 * <dl>
 * 	<dt>Scopes:
 * 	<dd>request</dd>
 * 	<dd>session</dd>
 *  <dd>application</dd>
 * 	</dt>
 * 	<dt>Modes:
 *  <dd>read</dd>
 *  <dd>write</dd>
 * </dl>
 * @author Michael Rimov
 *
 */
public final class PicoAccessPermission extends Permission implements Serializable {
	
	/**
	 * Serialization UUID
	 */
	private static final long serialVersionUID = 8652226940709137763L;

	public static final String REQUEST_SCOPE = "request";
	
	public static final String SESSION_SCOPE = "session";
	
	public static final String APP_SCOPE = "application";
	
	public static final String TRANSACTION_SCOPE = "transaction";
	
	public static final String WILDCARD_SCOPE = "*";
	
	public static final String READ = "read";
	
	public static final String WRITE = "write";
	
	/**
	 * bit-manageable constants
	 */
	public static final int READ_MODE = 1;
	
	/**
	 * bit-manageable constants
	 */
	public static final int WRITE_MODE = 2;
	
	private final int allowedMode;
	
	private static final String ERROR_EXAMPLE_MESSAGE = "Can only have permission modes of \"read\", \"write\" or \"read,write\"";
	
	private final List<String> scopes;
	

	public PicoAccessPermission(final String scopes, final String mode) {
		super(scopes);
		this.scopes = parseScopes(scopes);
		allowedMode = parseModes(mode);
	}


	private int parseModes(String mode) {
		if (mode == null) {
			throw new NullPointerException("mode");
		}
		
		int result = 0;
		StringTokenizer stok = new StringTokenizer(mode, ",");
		if (stok.countTokens() > 2) {
			throw new IllegalArgumentException(ERROR_EXAMPLE_MESSAGE);
		}
		
		while(stok.hasMoreTokens()) {
			String currentToken = stok.nextToken().trim();
			if (READ.equals(currentToken)) {
				if (result == READ_MODE) {
					throw new IllegalArgumentException("Cannot have permission mode \"read,read\"");
				}
				result = result + READ_MODE;
			} else if (WRITE.equals(currentToken)) {
				if (result == WRITE_MODE) {
					throw new IllegalArgumentException("Cannot have permission mode \"write,write\"");
				}
				
				result = result + WRITE_MODE;
			} else {
				throw new IllegalArgumentException("Unknown mode: '" + currentToken + " " + ERROR_EXAMPLE_MESSAGE);
			}
		}
		
		return result;
	}


	private List<String> parseScopes(String listedScopes) {
		if (listedScopes == null) {
			return Collections.emptyList();
		}
		
		List<String> result = new ArrayList<String>();
		StringTokenizer stok = new StringTokenizer(listedScopes, ",");
		while (stok.hasMoreTokens()) {
			result.add(stok.nextToken().trim());
		}
		
		return Collections.unmodifiableList(result);
	}



	@Override
	public boolean implies(Permission otherPermission) {
		if (!getClass().equals(otherPermission.getClass())) {
			return false;
		}
		
		PicoAccessPermission permission = (PicoAccessPermission)otherPermission;
		if (impliesScope(permission.scopes)) {
			if (impliesAccessMode(permission.allowedMode)) {
				return true;
			}
		}
		
		return false;
	}


	private boolean impliesAccessMode(int testMode) {
		if ( this.allowedMode == testMode) {
			return true;
		}
		
		if (this.allowedMode == READ_MODE + WRITE_MODE) {
			return true;
		}
		
		return false;
	}


	/**
	 * Returns true if all strings in 
	 * @param testMode
	 * @return
	 */
	private boolean impliesScope(List<String> testMode) {
		if (this.scopes.contains("*")) {
			return true;
		}
		
		for (String eachString : this.scopes) {
			if (!testMode.contains(eachString)) {
				return false;
			}
		}
		return true;
	}


	@Override
	public String getActions() {
		StringBuilder builder = new StringBuilder();
		boolean needComma = false;
		if ((this.allowedMode & READ_MODE) != 0) {
			builder.append(READ);
		}
		
		if ((this.allowedMode & READ_MODE) != 0) {
			if (needComma) {
				builder.append(",");
			}
			builder.append(WRITE);
		}
		return builder.toString();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + allowedMode;
		result = prime * result + ((scopes == null) ? 0 : scopes.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PicoAccessPermission other = (PicoAccessPermission) obj;
		if (allowedMode != other.allowedMode)
			return false;
		if (scopes == null) {
			if (other.scopes != null)
				return false;
		} else if (!scopes.equals(other.scopes))
			return false;
		return true;
	}}
