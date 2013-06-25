/**
 * This package contains the core API for PicoContainer, a compact container for working with the
 * <a href="http://www.martinfowler.com/articles/injection.html">dependency injection</a> pattern.
 * <p> When you use
 * PicoContainer for dependency injection, you create a new instance of
 * {@link com.picocontainer.MutablePicoContainer},
 * register classes (and possibly
 * {@link com.picocontainer.ComponentAdapter}s and component instances created through other means).
 * </p>
 * <p>
 * Object instances can then be accessed through the {@link com.picocontainer.PicoContainer} interface.
 * The container will create all
 * instances for you automatically, resolving their dependencies and order of instantiation.
 * The default container implementation is
 * the {@link com.picocontainer.DefaultPicoContainer} class.
 * </p>
 * <p>An extensive user guide,
 * a list of Frequently Asked Questions (FAQ) with answers and a lot more information is available from the
 * <a href="http://www.picocontainer.org/">PicoContainer</a> website.
 * You can also find various extensions, wrappers
 * and utility libraries that are based on this core API there.</p>
 *
 */
package com.picocontainer;