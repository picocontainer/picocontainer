package org.picocontainer.modules.deployer;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.script.ContainerBuilder;
import org.picocontainer.script.JdkScriptingContainerBuilder;
import org.picocontainer.script.LifecycleMode;

@SuppressWarnings("restriction")
public class JDKScriptingFileExtensionMapper implements FileExtensionMapper {

	private final ScriptEngineManager mgr;

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
				.get(0), scriptReader, cl, LifecycleMode.AUTO_LIFECYCLE);
	}
}
