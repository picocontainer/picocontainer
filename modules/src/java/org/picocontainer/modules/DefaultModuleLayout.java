package org.picocontainer.modules;

import javax.script.ScriptEngineManager;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.VFSClassLoader;

/**
 * The root folder to deploy must have the following file structure:
 * <pre>
 * +-someapp/
 *   +-META-INF/
 *   | +-picocontainer.[py|js|xml|bsh]
 *   +-com/
 *     +-blablah/
 *       +-Hip.class
 *       +-Hop.class
 * </pre>
 *
 * For those familiar with J2EE containers (or other containers for that matter), the
 * META-INF/picocontainer script is the ScriptedContainerBuilderFactory <em>composition script</em>. It plays the same
 * role as more classical "deployment descriptors", except that deploying via a full blown
 * scripting language is a lot more powerful!
 *
 * A new class loader (which will be a child of parentClassLoader) will be created. This classloader will make
 * the classes under the root folder available to the deployment script.
 *
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 *
 */
public class DefaultModuleLayout implements ModuleLayout {
	
	public static final String META_INF = "META-INF";
	private final String filebasename;

	/**
	 * 
	 * @param filebasename the root portion of the composition script name.  For example:  setting it to &quot;picocontainer&quot;
	 * would allow loading of META-INF/picocontainer.groovy, META-INF/
	 */
	public DefaultModuleLayout(String filebasename) {
		this.filebasename = filebasename;
		
	}

	public FileObject getDeploymentScript(FileObject applicationFolder, ScriptEngineManager mgr) throws FileSystemException, DeploymentException {
        final FileObject metaInf = applicationFolder.getChild(META_INF);
        if(metaInf == null) {
            throw new DeploymentException("Missing "+META_INF+" folder in " + applicationFolder.getName().getPath());
        }
        
        //Perform a find files that only checks the first level of depth
        final FileObject[] picoContainerScripts = metaInf.findFiles(new FileSelector(){

            public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
                return fileSelectInfo.getFile().getName().getBaseName().startsWith(getFileBasename());
            }

            public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
              //
              //picocontainer.* can easily be deep inside a directory tree and
              //we end up not picking up our desired script.
              //
                return fileSelectInfo.getDepth() <= 1;
            }
        });

        if(picoContainerScripts == null || picoContainerScripts.length < 1) {
            throw new DeploymentException("No deployment script ("+ getFileBasename() +".[groovy|bsh|js|py|xml]) in " + applicationFolder.getName().getPath() + "/META-INF");
        }

        if (picoContainerScripts.length == 1) {
        	//Thought -- what would happen if we allowed multiple scripts?
          return picoContainerScripts[0];
        } else {
          throw new DeploymentException("Found more than one candidate config script in : " + applicationFolder.getName().getPath() + "/"+ META_INF +"."
              + "Please only have one " + getFileBasename() + ".[groovy|bsh|js|py|xml] this directory.");
        }

		
	}

	private String getFileBasename() {
		return this.filebasename;
	}

	public ClassLoader constructModuleClassLoader(
			ClassLoader parentClassLoader, FileObject applicationFolder) throws FileSystemException {
		return new VFSClassLoader(applicationFolder, applicationFolder.getFileSystem().getFileSystemManager(), parentClassLoader);
	}

}
