package com.picocontainer.modules.deployer;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import com.picocontainer.script.ContainerBuilder;
import com.picocontainer.script.JdkScriptingContainerBuilder;
import com.picocontainer.script.NoOpPostBuildContainerAction;

@SuppressWarnings("restriction")
public class JDKScriptingFileExtensionMapper implements FileExtensionMapper {

	private final ScriptEngineManager mgr;

	/**
	 * Default constructor that creates its own script manager instance.
	 */
	public JDKScriptingFileExtensionMapper() {
		this(new ScriptEngineManager());
	}
	
	/**
	 * Constructor that allows sharing of script engine manager instances.
	 * @param mgr
	 */
	public JDKScriptingFileExtensionMapper(final ScriptEngineManager mgr) {
		this.mgr = mgr;
	}

	/** {@inheritDoc} **/
	public boolean isExtensionAKnownScript(final String fileExtension) {
		if (fileExtension == null || fileExtension.length() == 0) {
			return false;
		}

		return (mgr.getEngineByExtension(fileExtension) != null);
	}

	/** {@inheritDoc} **/
	public String getAllSupportedExtensions() {
		final StringBuilder result = new StringBuilder();
		boolean needPipe = false;
		for (final ScriptEngineFactory eachFactory : mgr.getEngineFactories()) {
			for (final String eachExtension : eachFactory.getExtensions()) {
				if (needPipe) {
					result.append("|");
				} else {
					needPipe = true;
				}

				result.append(eachExtension);
			}
		}

		return result.toString();
	}

	/** {@inheritDoc} **/
	public ContainerBuilder instantiateContainerBuilder(final ClassLoader cl,
			final FileObject script) throws FileSystemException {
		final Reader scriptReader = new InputStreamReader(script.getContent()
				.getInputStream());
		final ScriptEngine engine = mgr.getEngineByExtension(script.getName()
				.getExtension());
		return new JdkScriptingContainerBuilder(engine.getFactory().getNames()
				.get(0), scriptReader, cl).setPostBuildAction(new NoOpPostBuildContainerAction()) ;
	}
}
