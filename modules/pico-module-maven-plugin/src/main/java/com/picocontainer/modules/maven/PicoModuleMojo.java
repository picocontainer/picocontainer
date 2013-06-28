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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Build a JAR from the current project.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: PicoModuleMojo.java 1211837 2011-12-08 11:23:22Z bimargulies $
 * @goal package
 * @phase package
 * @requiresProject
 * @threadSafe
 * @requiresDependencyResolution runtime
 */
public class PicoModuleMojo
    extends AbstractJarMojo
{
    /**
     * Directory containing the classes and resource files that should be packaged into the JAR.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be attached.
     * If this is not given,it will merely be written to the output directory
     * according to the finalName.
     *
     * @parameter
     */
    private String classifier;

    protected String getClassifier()
    {
        return classifier;
    }

    /**
     * @return type of the generated artifact
     */
    protected String getType()
    {
        return "pico-module";
    }

    /**
     * Return the main classes directory, so it's used as the root of the jar.
     */
    protected File getClassesDirectory()
    {
        return classesDirectory;
    }
    
    /**
     * Generates the Pico Archive.
     *
     * @todo Add license files in META-INF directory.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipIfEmpty && !getClassesDirectory().exists() )
        {
            getLog().info( "Skipping packaging of module because " + getClassesDirectory().getAbsolutePath() + " doesn't exist" );
        }
        else
        {
            File jarFile = createArchive();

            String classifier = getClassifier();
            if ( classifier != null )
            {
                projectHelper.attachArtifact( getProject(), getType(), classifier, jarFile );
            }
            else
            {
                getProject().getArtifact().setFile( jarFile );
            }
        }
    }    
}
