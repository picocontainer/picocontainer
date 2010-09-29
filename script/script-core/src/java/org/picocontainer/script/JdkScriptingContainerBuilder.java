package org.picocontainer.script;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

/**
 * Builds a PicoContainer using the JDK 1.6 scripting engine. If you are stuck
 * with JDK 1.5 you can use the other scripting mechanisms that Pico Scripting
 * provides listed <a href="http://picocontainer.org/script/">here</a>.
 * <p>
 * To use a script engine, you must either use the JDK 1.6 built-in Javascript
 * engine (Mozilla Rhino) or have another scripting engine in your classpath.
 * </p>
 * <p>
 * A list of well known scripting engines are <a
 * href="https://scripting.dev.java.net">here</a>
 * </p>
 * 
 * @author Michael Rimov, Centerline Computers, Inc.
 */
@SuppressWarnings("restriction")
// Why Eclipse complains about this, I don't understand
public class JdkScriptingContainerBuilder extends ScriptedContainerBuilder {

	private final String engineName;
	
	/**
	 * Constructs a scripting engine based on the engine's short name.
	 * @param scriptEngineShortName the &quot;short name&quot; of the scripting
	 * engine you wish to use.  An example is 'js' (without quotes) for the Javascript
	 * engine.
	 * @param script
	 * @param classLoader
	 * @param lifecycleMode
	 */
	public JdkScriptingContainerBuilder(final String scriptEngineShortName,
			final Reader script, final ClassLoader classLoader,
			final LifecycleMode lifecycleMode) {
		super(script, classLoader, lifecycleMode);
		this.engineName = scriptEngineShortName;
	}

	public JdkScriptingContainerBuilder(final String scriptEngineShortName,
			final Reader script, final ClassLoader classLoader) {
		super(script, classLoader);
		engineName = scriptEngineShortName;
	}

	public JdkScriptingContainerBuilder(final String scriptEngineShortName,
			final URL script, final ClassLoader classLoader,
			final LifecycleMode lifecycleMode) {
		super(script, classLoader, lifecycleMode);
		engineName = scriptEngineShortName;
	}

	public JdkScriptingContainerBuilder(final String scriptEngineShortName,
			final URL script, final ClassLoader classLoader) {
		super(script, classLoader);
		engineName = scriptEngineShortName;
	}

	@Override
	protected PicoContainer createContainerFromScript(
			final PicoContainer parentContainer, final Object assemblyScope) {
		final ScriptEngineManager mgr = new ScriptEngineManager();
		final ScriptEngine engine = mgr.getEngineByName(engineName);
		if (engine == null) {
			final StringBuilder message = new StringBuilder(
					"Could not find a script engine named: '" + engineName
							+ "' all script engines in your classpath are:\n");
			for (final ScriptEngineFactory eachFactory : mgr
					.getEngineFactories()) {
				message.append("\t Engine named '"
						+ eachFactory.getEngineName()
						+ "' which supports the language '"
						+ eachFactory.getLanguageName()
						+ "' with short names '"
						+ Arrays.toString(eachFactory.getNames().toArray())
						+ "'\n");
			}

			throw new PicoCompositionException(message.toString());
		}

		final Bindings bindings = engine.createBindings();
		bindings.put("parent", parentContainer);
		bindings.put("assemblyScope", assemblyScope);
		applyOtherBindings(bindings);

		Reader reader = null;
		try {
			reader = this.getScriptReader();
			PicoContainer result = (PicoContainer) engine
					.eval(reader, bindings);
			if (result == null) {
				result = (PicoContainer) bindings.get("pico");
				if (result == null) {
					result = (PicoContainer) bindings.get("nano");
					if (result == null) {
						throw new PicoCompositionException(
								"Script completed successfully, but did not return any value, nor did it declare a variable named 'pico'");
					}
				}
			}
			return result;
		} catch (final ClassCastException e) {
			throw new ScriptedPicoContainerMarkupException(
					"The return type of the script must be of type PicoContainer",
					e);
		} catch (final IOException e) {
			throw new ScriptedPicoContainerMarkupException(
					"IOException encountered, message -'" + e.getMessage()
							+ "'", e);
		} catch (final ScriptException e) {
			throw new ScriptedPicoContainerMarkupException(
					"Error executing composition script under engine '"
							+ engine.getFactory().getEngineName() + "'", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					// Ignore
				}
			}
		}
	}

	/**
	 * Allows other bindings to be managed by the descendent implementations.
	 * Examples would be servlet requests/responses, JNDI contexts, etc.
	 * 
	 * @param bindings
	 */
	protected void applyOtherBindings(final Bindings bindings) {

	}

}
