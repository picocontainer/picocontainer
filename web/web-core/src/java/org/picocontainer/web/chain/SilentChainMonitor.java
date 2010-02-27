package org.picocontainer.web.chain;

/**
 * chain monitor displaying nothing
 * @author Konstantin Priobluda
 *
 */
public class SilentChainMonitor implements ChainMonitor {

	public void filteringURL(String originalUrl) {
	}

	public void exceptionOccurred(Exception e) {
	}

	public void pathAdded(String path, String url) {
	}

}
