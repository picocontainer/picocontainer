package com.picocontainer.modules.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * 
 * @author Michael Rimov
 * @goal inplace
 * @requiresProject
 * @threadSafe
 * @requiresDependencyResolution runtime
 */
public class InPlaceModuleMojo extends AbstractInplaceMojo {

	/**
	 * Sets the target directory where modules will be placed.
	 * @parameter expression="${basedir}/src/main/webapp/WEB-INF/modules
	 * @required
	 */
	private File inplaceDirectory;

	/**
	 * Copies the modules into the main directory structure
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			assert getModuleDirectory() != null;
			getLog().info("Copying Modules into " + this.getModuleDirectory().getAbsolutePath());
			
			if (!getModuleDirectory().exists()) {
				if (!getModuleDirectory().mkdirs()) {
					throw new MojoFailureException("Cannot make directory " + getModuleDirectory().getAbsolutePath());
				}
			}
			
			for (Artifact eachArtifact : getModuleArtifacts()) {
		        if (this.isExpanded()) {
		            File dest = new File(getModuleDirectory(), eachArtifact.getGroupId() + "." + eachArtifact.getArtifactId());
		            if (!dest.exists()) {
		            	if (!dest.mkdir()) {
		            		throw new IOException("Unable to create directory " + dest.getPath());
		            	}
		            }
		        	getLog().debug("Unpacking" +  eachArtifact.getFile().getPath() + " into directory " + dest.getPath());
		        	unpack(eachArtifact.getFile(), dest);
		        } else {
		        	FileUtils.copyFile(eachArtifact.getFile(), getModuleDirectory());
		        }				
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error copying dependencies", e);
		}		
		
	}

	public File getModuleDirectory() {
		return inplaceDirectory;
	}

	public void setModuleDirectory(File moduleDirectory) {
		this.inplaceDirectory = moduleDirectory;
	}


}
