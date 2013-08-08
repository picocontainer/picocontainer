package com.picocontainer.modules.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * 
 * @goal copy-modules
 * @requiresProject
 * @threadSafe
 * @requiresDependencyResolution runtime
 */
public class CopyModules extends AbstractInplaceMojo {

	/**
	 * Sets the target directory where modules will be placed. It will have a
	 * default that is sensible for webapps, but useful for any sort of
	 * packaging.
	 * 
	 * @parameter expression="${project.build.directory}/${project.build.finalName}/WEB-INF/modules
	 * @required
	 */
	private File targetModuleDirectory;

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
					File dest = new File(getModuleDirectory(), eachArtifact.getGroupId() + "."
							+ eachArtifact.getArtifactId());
					if (!dest.exists()) {
						if (!dest.mkdir()) {
							throw new IOException("Unable to create directory " + dest.getPath());
						}
					}
					getLog().debug("Unpacking" + eachArtifact.getFile().getPath() + " into directory " + dest.getPath());
					unpack(eachArtifact.getFile(), dest);
				} else {
					FileUtils.copyFile(eachArtifact.getFile(), getModuleDirectory());
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error copying dependencies", e);
		}
	}

	@Override
	public File getModuleDirectory() {
		return targetModuleDirectory;
	}

}
