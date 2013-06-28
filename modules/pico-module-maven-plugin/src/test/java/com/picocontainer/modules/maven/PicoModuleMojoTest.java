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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.modules.maven.PicoModuleMojo;

/**
 * Test for {@link PicoModuleMojo}
 *
 * @version $Id: PicoModuleMojoTest.java 728546 2008-12-21 22:56:51Z bentmann $
 */
public class PicoModuleMojoTest extends AbstractMojoTestCase
{
    private File testPom = null; 

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
        testPom = getTestFile( "src/test/resources/unit/basic-module-test/pom.xml" );
    }
    
    @After
    public void tearDown() throws Exception {
    	super.tearDown();
    }

    /**
     * tests the proper discovery and configuration of the mojo
     *
     * @throws Exception
     */
    @Test
    public void testPicoModuleTestEnvironment()
        throws Exception
    {

        PicoModuleMojo mojo = (PicoModuleMojo) lookupMojo( "package", testPom );

        assertNotNull( mojo );

        assertEquals( "foo", mojo.getProject().getGroupId() );
        assertEquals("bar", mojo.getProject().getArtifactId());
    }
    
    @Test
    public void testPicoModulePreArchiveStep() throws Exception {
        PicoModuleMojo mojo = (PicoModuleMojo) lookupMojo( "package", testPom );
        
        File targetFile = mojo.buildExplodedArchive();
        assertNotNull(targetFile);
        assertTrue(targetFile.exists());
        
        File metaInfDir = new File(targetFile, "META-INF");
        assertTrue(metaInfDir.exists());
        
        File compositionFile = new File(metaInfDir, "composition.groovy");
        assertTrue(compositionFile.exists());
        
        File classFile = new File(targetFile, "com/picocontainer/modules/maven/PicoModuleMojo.class");
        assertTrue(classFile.exists());
    }

}
