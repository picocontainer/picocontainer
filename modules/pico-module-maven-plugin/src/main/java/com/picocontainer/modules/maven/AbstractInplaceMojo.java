package com.picocontainer.modules.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

abstract public class AbstractInplaceMojo extends AbstractMojo {

	public static final String DEPENDENCY_TYPE = "pico-module";
	
    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
	
	
	/**
	 * If set to true, then all the modules will be expanded into the target directory.
	 * @parameter default-value="true"
	 */
	private boolean expanded;


    /**
     * To look up Archiver/UnArchiver implementations
     *
     * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
     * @required
     */
    protected ArchiverManager archiverManager;	
	
	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	abstract public File getModuleDirectory();


	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}	
	
	
    /**
     * Unpacks the archive file.
     *
     * @param file     File to be unpacked.
     * @param location Location where to put the unpacked files.
     */
    protected void unpack(File file, File location) throws MojoExecutionException {

        try {
            location.mkdirs();

            UnArchiver unArchiver = archiverManager.getUnArchiver("jar");

            unArchiver.setSourceFile(file);

            unArchiver.setDestDirectory(location);

            unArchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unknown archiver type", e);
        } catch (ArchiverException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error unpacking file: " + file + " to: " + location + "\r\n"
                + e.toString(), e);
        }
    }	
    
    
	
    /**
     * Get the artifacts that are of type 'pico-module' that are listed in the pom.
     * @return
     */
	protected List<Artifact> getModuleArtifacts() {
		List<Artifact> result = new ArrayList<Artifact>();
	
		for (@SuppressWarnings("unchecked") Iterator<Artifact> i = getProject().getArtifacts().iterator(); i.hasNext();) {
			Artifact dep = (Artifact)i.next();
		    if (DEPENDENCY_TYPE.equals(dep.getType())) {
		    	result.add(dep);
		    }
		}
		
		return result;
	}    

}
