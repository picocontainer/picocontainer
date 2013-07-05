package com.picocontainer.modules.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Cleans only the pico-modules listed in the pom.  If you wish to fork a module then copy it with :inplace target
 * then remove the reference from the pom.xml.
 * @author Mike
 * @goal clean
 * @requiresProject
 * @threadSafe
 * @requiresDependencyResolution runtime
 */
public class CleanMojo extends AbstractInplaceMojo {

	/**
	 * Sets the target directory where modules will be placed.
	 * @parameter expression="${basedir}/src/main/webapp/WEB-INF/modules
	 * @required
	 */
	private File inplaceDirectory;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Removing modules from source directory: " + this.getModuleDirectory().getPath() + ".....");
        IOException lastIoException = null;
		for (Artifact eachArtifact : getModuleArtifacts()) {
			File expandedModuleFile = getExpandedModuleFile(eachArtifact);
			if (expandedModuleFile.exists()) {
				try {
					FileUtils.deleteDirectory(expandedModuleFile);
				} catch (IOException e) {
					lastIoException = e;
					getLog().error("Unable to clean file " + expandedModuleFile.getPath());
				}
			} else {
				File compressedModuleFile = getCompressedModuleFile(eachArtifact);
				if(!compressedModuleFile.exists()) {
					getLog().info("Module for " + eachArtifact.getGroupId() + "." + eachArtifact.getArtifactId()  + " doesn't appear to exist.... skipping processing");
					continue;
				}
				if (compressedModuleFile.delete()) {
					getLog().debug("Cleaned file " + compressedModuleFile.getPath());
				} else {
					getLog().error("Unable to clean file " + compressedModuleFile.getPath());
				}
			}
		}		
		
		if (lastIoException != null) {
			throw new MojoExecutionException("Unable to clean all modules.", lastIoException);
		}
        
	}

	private File getExpandedModuleFile(Artifact eachArtifact) {
		return new File(this.getModuleDirectory(), eachArtifact.getGroupId() + "." + eachArtifact.getArtifactId());
	}

	private File getCompressedModuleFile(Artifact eachArtifact) {
		return new File(getModuleDirectory(),  eachArtifact.getGroupId() + "." + eachArtifact.getArtifactId() + ".pico-module");
	}

	@Override
	public File getModuleDirectory() {
		return this.inplaceDirectory;
	}
	
	

}
