package com.picocontainer.modules.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for creating a module from project classes.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: AbstractModuleMojo.java 1235468 2012-01-24 20:22:30Z krosenvold $
 */
public abstract class AbstractModuleMojo
    extends AbstractMojo
{

    private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html", "**/doc-files/*" };

    private static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };

    /**
     * List of files to include. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the Module.
     *
     * @parameter
     */
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the Module.
     *
     * @parameter
     */
    private String[] excludes;

    /**
     * Directory containing the generated Module.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Name of the generated module.
     *
     * @parameter alias="jarName" expression="${pico-module.finalName}" default-value="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * The Jar archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     */
    private JarArchiver jarArchiver;

    /**
     * The Maven project.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${session}"
     * @readonly
     * @required
     */
    private MavenSession session;

    /**
     * The archive configuration to use.
     * See <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver Reference</a>.
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Path to the default MANIFEST file to use. It will be used if
     * <code>useDefaultManifestFile</code> is set to <code>true</code>.
     *
     * @parameter default-value="${project.build.outputDirectory}/META-INF/MANIFEST.MF"
     * @required
     * @readonly
     */
    private File defaultManifestFile;

    /**
     * Set this to <code>true</code> to enable the use of the <code>defaultManifestFile</code>.
     *
     * @parameter expression="${pico-module.useDefaultManifestFile}" default-value="false"
     *
     */
    private boolean useDefaultManifestFile;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * Whether creating the archive should be forced.
     *
     * @parameter expression="${pico-module.forceCreation}" default-value="false"
     */
    private boolean forceCreation;
	
    /**
     * Skip creating empty archives
     * 
     * @parameter expression="${pico-module.skipIfEmpty}" default-value="false"
     */
    protected boolean skipIfEmpty;
    
    /**
     * Composition Script:  If not set, then its assumed
     * that a composition java class is used.
     * @parameter
     */
    private String compositionFile;

    /**
     * Return the specific output directory to serve as the root for the archive.
     */
    protected abstract File getClassesDirectory();
    
    protected File getCompositionFileAsFile() throws MojoExecutionException {
    	File f = new File(project.getBasedir(), compositionFile);
    	if (! f.exists()) {
    		throw new MojoExecutionException("Can't find composition file " + f);
    	}
    	return f;
    }

    protected final MavenProject getProject()
    {
        return project;
    }

    /**
     * Overload this to produce a pico module with another classifier, for example a test-module.
     */
    protected abstract String getClassifier();

    /**
     * Overload this to produce a test-module, for example.
     */
    protected abstract String getType();

    protected static File getJarFile( File basedir, String finalName, String classifier )
    {
        if ( classifier == null )
        {
            classifier = "";
        }
        else if ( classifier.trim().length() > 0 && !classifier.startsWith( "-" ) )
        {
            classifier = "-" + classifier;
        }

        return new File( basedir, finalName + classifier + ".pico-module" );
    }

    /**
     * Default Manifest location. Can point to a non existing file.
     * Cannot return null.
     */
    protected File getDefaultManifestFile()
    {
        return defaultManifestFile;
    }


    protected File buildExplodedArchive() throws MojoExecutionException, IOException {
    	getLog().info("Copying Archive Files" );
    	File explodedDirectory = new File(outputDirectory, "archive");
    	explodedDirectory.mkdirs();
    	
        File contentDirectory = getClassesDirectory();
        if ( !contentDirectory.exists() )
        {
            getLog().warn( "Pico Archive will be empty - no content was marked for inclusion!" );
        }
        else
        {
            FileUtils.copyDirectoryStructure(contentDirectory, explodedDirectory);
        }
    	
        if (this.compositionFile != null) {
            File metaInfDir = new File(explodedDirectory, "META-INF");
            getLog().debug("Copying " + getCompositionFileAsFile().getAbsolutePath() + " to " + metaInfDir.getAbsolutePath());
            if (!metaInfDir.exists() &&  !metaInfDir.mkdirs()) {
            	getLog().error("Unable to create target directory " + metaInfDir.getAbsolutePath());
            }
            File compositionFile = getCompositionFileAsFile();
            FileUtils.copyFile(compositionFile, new File(metaInfDir, compositionFile.getName()) );
        } else {
        	getLog().info("'compositionFile' has not been set.  Assuming that you are composition the module with a class named " 
        				+ getProject().getGroupId() + "." 
        				+ getProject().getArtifactId() + ".Composition");
        }
        
        
    	return explodedDirectory;
    }
    
    /**
     * Generates the Module.
     *
     * @todo Add license files in META-INF directory.
     */
    public File createArchive()
        throws MojoExecutionException
    {
        File picoArchive = getJarFile( outputDirectory, finalName, getClassifier() );

        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver( jarArchiver );

        archiver.setOutputFile( picoArchive );

        archive.setForced( forceCreation );

        try
        {
        	File contentDirectory =  buildExplodedArchive();        	
            if ( !contentDirectory.exists() )
            {
                getLog().warn( "Module will be empty - no content was marked for inclusion!" );
            }
            else
            {
                archiver.getArchiver().addDirectory( contentDirectory, getIncludes(), getExcludes() );
            }


            
            
            File existingManifest = getDefaultManifestFile();

            if ( useDefaultManifestFile && existingManifest.exists() && archive.getManifestFile() == null )
            {
                getLog().info( "Adding existing MANIFEST to archive. Found under: " + existingManifest.getPath() );
                archive.setManifestFile( existingManifest );
            }

            
            
            archiver.createArchive( session, project, archive );

            return picoArchive;
        }
        catch ( Exception e )
        {
            // TODO: improve error handling
            throw new MojoExecutionException( "Error assembling Pico Module", e );
        }
    }



    private String[] getIncludes()
    {
        if ( includes != null && includes.length > 0 )
        {
            return includes;
        }
        return DEFAULT_INCLUDES;
    }

    private String[] getExcludes()
    {
        if ( excludes != null && excludes.length > 0 )
        {
            return excludes;
        }
        return DEFAULT_EXCLUDES;
    }

}
