package org.picocontainer.modules;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngineManager;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.script.ContainerBuilder;
import org.picocontainer.script.ScriptedBuilderNameResolver;
import org.picocontainer.script.ScriptedContainerBuilderFactory;
import org.picocontainer.script.UnsupportedScriptTypeException;

/**
 * This class is capable of deploying an application from any kind of file system
 * supported by <a href="http://jakarta.apache.org/commons/sandbox/vfs/">Jakarta VFS</a>.
 * (Like local files, zip files etc.) - following the ScriptedContainerBuilderFactory scripting model.
 *
 * IMPORTANT NOTE:
 * The scripting engine (rhino, jython, groovy etc.) should be loaded by the same classLoader as
 * the appliacation classes, i.e. the VFSClassLoader pointing to the app directory.
 *
 * <pre>
 *    +-------------------+
 *    | xxx               |  <-- parent app loader (must not contain classes from app builder classloader)
 *    +-------------------+
 *              |
 *    +-------------------+
 *    | someapp           | <-- app classloader (must not contain classes from app builder classloader)
 *    +-------------------+
 *              |
 *    +-------------------+
 *    | picocontainer     |
 *    | nanocontainer     |  <-- app builder classloader
 *    | rhino             |
 *    | jython            |
 *    | groovy            |
 *    +-------------------+
 * </pre>
 *
 * This means that these scripting engines should *not* be accessible by any of the app classloader, since this
 * may prevent the scripting engine from seeing the classes loaded by the VFSClassLoader. In other words,
 * the scripting engine classed may be loaded several times by different class loaders - once for each
 * deployed application.
 *
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 */
public class PicoContainerDeployer implements Deployer {

    /**
     * File Name to builder class name resolver.
     */
    private ScriptedBuilderNameResolver resolver;

    /**
     * Module definition to be deployed
     */
	private final ModuleLayout moduleLayout;


    /**
     * Default constructor that makes sensible defaults.
     * @throws FileSystemException
     */
    public PicoContainerDeployer() throws FileSystemException {
        this(new ScriptedBuilderNameResolver(), new DefaultModuleLayout("picocontainer"));
    }

    /**
     * Constructs this object with both a VFS file system manager, and
     * @param fileSystemManager FileSystemManager
     * @param builderResolver ScriptBuilderResolver
     */
    public PicoContainerDeployer(ScriptedBuilderNameResolver builderResolver, ModuleLayout layout) {
        this(layout);
        resolver = builderResolver;
    }

    /**
     * Constructs a picocontainer deployer with the specified file system manager
     * and specifies a 'base name' for the configuration file that will be loaded.
     * @param fileSystemManager A VFS FileSystemManager.
     * @todo Deprecate this and replace 'base file name' with the concept
     * of a ArchiveLayout that defines where jars are stored, where the composition
     * script is stored, etc.
     * @param baseFileName
     */
    public PicoContainerDeployer( ModuleLayout moduleLayout) {
		this.moduleLayout = moduleLayout;
        resolver = new ScriptedBuilderNameResolver();
        
    }


    @SuppressWarnings("restriction")
	public MutablePicoContainer deploy(FileObject applicationFolder, ClassLoader parentClassLoader, MutablePicoContainer parentContainer, Object assemblyScope) throws FileSystemException {
        ClassLoader applicationClassLoader = moduleLayout.constructModuleClassLoader(parentClassLoader, applicationFolder);

		final ScriptEngineManager mgr = new ScriptEngineManager();
        FileObject deploymentScript = moduleLayout.getDeploymentScript(applicationFolder, mgr);

        String extension = "." + deploymentScript.getName().getExtension();
        Reader scriptReader = new InputStreamReader(deploymentScript.getContent().getInputStream());
        String builderClassName;
        try {
            builderClassName = resolver.getBuilderClassName(extension);
        } catch (UnsupportedScriptTypeException ex) {
            throw new FileSystemException("Could not find a suitable builder for: " + deploymentScript.getName()
                + ".  Known extensions are: [groovy|bsh|js|py|xml]", ex);
        }


        ScriptedContainerBuilderFactory scriptedContainerBuilderFactory = new ScriptedContainerBuilderFactory(scriptReader, builderClassName, applicationClassLoader);
        ContainerBuilder builder = scriptedContainerBuilderFactory.getContainerBuilder();

        return (MutablePicoContainer) builder.buildContainer(parentContainer, assemblyScope, true);
    }

}
