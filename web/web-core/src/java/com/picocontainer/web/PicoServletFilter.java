package com.picocontainer.web;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.picocontainer.MutablePicoContainer;

@SuppressWarnings("serial")
public class PicoServletFilter extends AbstractPicoServletContainerFilter {

	private static transient ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();
	private static transient ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
	private static transient ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();

	protected final void setAppContainer(MutablePicoContainer container) {
		currentAppContainer.set(container);
	}

	protected final void setRequestContainer(MutablePicoContainer container) {
		currentRequestContainer.set(container);
	}

	protected final void setSessionContainer(MutablePicoContainer container) {
		currentSessionContainer.set(container);
	}

	protected final MutablePicoContainer getApplicationContainer() {
		MutablePicoContainer pico =currentAppContainer.get();
		if (pico == null) {
			throw new PicoContainerWebException(
					"No request container has been set.  Is PicoServletContainerFilter installed in your web.xml?  "
							+ "And if it is, is exposeServletInfrastructure set to true in filter init parameters?");
		}

		return pico;
	}

	protected final MutablePicoContainer getSessionContainer() {
		MutablePicoContainer pico = currentSessionContainer.get();
		if (pico == null) {
			throw new PicoContainerWebException(
					"No request container has been set.  Is PicoServletContainerFilter installed in your web.xml?  "
							+ "And if it is, is exposeServletInfrastructure set to true in filter init parameters?");
		}

		return pico;
	}

	/**
	 * May return null!
	 * 
	 * @return
	 */
	protected final MutablePicoContainer getApplicationContainerWithoutException() {
		return currentAppContainer.get();
	}

	/**
	 * May return null!
	 * 
	 * @return
	 */
	protected final MutablePicoContainer getRequestContainerWithoutException() {
		return currentRequestContainer.get();
	}

	protected final MutablePicoContainer getRequestContainer() {
		MutablePicoContainer result = currentRequestContainer.get();
		if (result == null) {
			throw new PicoContainerWebException(
					"No request container has been set.  Is PicoServletContainerFilter installed in your web.xml?  "
							+ "And if it is, is exposeServletInfrastructure set to true in filter init parameters?");
		}

		return result;
	}

	public void destroy() {
		if (currentRequestContainer != null) {
			currentRequestContainer.remove();
		}

		if (currentSessionContainer != null) {
			currentSessionContainer.remove();
		}

		if (currentAppContainer != null) {
			currentAppContainer.remove();
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Initializes static variables if necessary.  This method is synchronized
	 * on the object's class to prevent static race conditions.
	 */
	private void checkConstructed() {
		synchronized (PicoServletFilter.class) {
			if (currentRequestContainer == null) {

				currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
			}

			if (currentRequestContainer == null) {
				currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
			}

			if (currentSessionContainer == null) {
				currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
			}
		}
	}

	/**
	 * Check to see if static re-initialization is necessary.
	 * @param ois
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		checkConstructed();
	}

}