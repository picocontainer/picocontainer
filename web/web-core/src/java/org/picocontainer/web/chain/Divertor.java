package org.picocontainer.web.chain;

/**
 * generates diversion URL for given cause
 * @author k.pribluda
 */
public interface Divertor {
	/**
	 * find path to divert to, or null can not be found
	 * @param cause cause of diversion
	 * @return servlet path or null
	 */
	String divert(Throwable cause);
}
