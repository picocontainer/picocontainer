/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.script;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.picocontainer.PicoContainer;

/**
 * Standalone offers a command line interface to PicoContainer.
 * Standalone options are: -c <composition-file> [-q|-n|-h|-v]
 * <ul>
 *  <li>-c: specifies composition file</li>
 *  <li>-q: quite mode</li>
 *  <li>-n: forces ScriptedContainerBuilderFactory to exit after start</li>
 *  <li>-h: print usage</li>
 *  <li>-v: print version</li>
 * </ul>
 */
public class Standalone {

    private static final char HELP_OPT = 'h';
    private static final char VERSION_OPT = 'v';
    private static final char COMPOSITION_OPT = 'c';
    private static final char RESOURCE_OPT = 'r';
    private static final char QUIET_OPT = 'q';
    private static final char NOWAIT_OPT = 'n';

    private static final String DEFAULT_COMPOSITION_FILE = "composition.groovy";

    static final Options createOptions() {
        Options options = new Options();
        options.addOption(String.valueOf(HELP_OPT), "help", false,
                "print this message and exit");
        options.addOption(String.valueOf(VERSION_OPT), "version", false,
                "print the version information and exit");
        options.addOption(String.valueOf(COMPOSITION_OPT), "composition", true,
                "specify the composition file");
        options.addOption(String.valueOf(RESOURCE_OPT), "resource", true,
                "specify the composition file (as a resource read from classpath - like inside a jar)");
        options.addOption(String.valueOf(QUIET_OPT), "quiet", false,
                "forces ScriptedContainerBuilderFactory to be quiet");
        options.addOption(String.valueOf(NOWAIT_OPT), "nowait", false,
                "forces ScriptedContainerBuilderFactory to exit after start");
        return options;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
		new Standalone(args);
    }

    public Standalone(String[] args) throws IOException, ClassNotFoundException {
        File defaultCompositionFile = new File(DEFAULT_COMPOSITION_FILE);
        CommandLine cl = null;
        Options options = createOptions();
        if (args.length == 0 && !defaultCompositionFile.exists()) {
            printUsage(options);
            System.exit(-1);
         }
        try {
            cl = getCommandLine(args, options);
        } catch (ParseException e) {
            System.out.println("PicoContainer Standalone: Error in parsing arguments: ");
            e.printStackTrace();
            System.exit(-1);
        }

        if (cl.hasOption(HELP_OPT)) {
            printUsage(options);
            System.exit(0);
        }
        if (cl.hasOption(VERSION_OPT)) {
            printVersion();
            System.exit(0);
        }

        boolean quiet = cl.hasOption(QUIET_OPT);
        boolean nowait = cl.hasOption(NOWAIT_OPT);
        try {
            String compositionFile = cl.getOptionValue(COMPOSITION_OPT);
            String compositionResource = cl.getOptionValue(RESOURCE_OPT);
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            if (compositionFile != null) {
                buildAndStartContainer(new File(compositionFile), quiet, nowait);
            } else if (compositionResource != null) {
                buildAndStartContainer(Standalone.class.getResource(compositionResource), quiet, nowait);
            } else {
                if (defaultCompositionFile.exists()) {
                    buildAndStartContainer(defaultCompositionFile, quiet, nowait);
                } else {
                    printUsage(options);
                    System.exit(10);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("PicoContainer Standalone: Failed to start application. Cause : " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (ClassNotFoundException e) {
            System.err.println("PicoContainer Standalone: Failed to start application. A Class was not found. Exception message : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        if (!quiet) {
            System.out.println("PicoContainer Standalone: Exiting main method.");
        }
    }


    /*
    Now that the breadth/depth-first traversal of "child" containers, we should consider adding support
    for "monitors" at a higher level of abstraction.

    I think that ideally this should be done on the multicaster level, so that we can get monitor
    events whenever *any* method is called via the multicaster. That way we could easily intercept lifecycle
    methods on individual components, not only on the container level.

    The most elegant way to deal with this is perhaps via Nanning, or we could add support for it
    directly in the MulticastInvoker class. (It could be constructed with an additional argument
    called InvocationInterceptor. MulticastInvoker would then call methods on this object in addition
    to the subject. The InvocationInterceptor would serve the same purpose as this PicoContainer,
    but at a much higher level of abstraction. It would be more reusable, since it would enable monitoring
    outside the scope of nano. It could be useful in e.g. WebWork or other environments.

    I think it should be up to the ContainerComposer instances (in integrationkit) to decide what kind of
    monitor/InvocationInterceptor to use.

    AH
    */
    private static void buildAndStartContainer(URL composition, final boolean quiet, boolean nowait) throws ClassNotFoundException {
        final ScriptedContainerBuilderFactory scriptedContainerBuilderFactory = new ScriptedContainerBuilderFactory(composition);
        buildContainer(scriptedContainerBuilderFactory, nowait, quiet);
    }

    private static void buildAndStartContainer(File composition, boolean quiet, boolean nowait) throws IOException, ClassNotFoundException {
        final ScriptedContainerBuilderFactory scriptedContainerBuilderFactory = new ScriptedContainerBuilderFactory(composition);
        buildContainer(scriptedContainerBuilderFactory, nowait, quiet);
    }


    private static void buildContainer(final ScriptedContainerBuilderFactory scriptedContainerBuilderFactory, boolean nowait, final boolean quiet) {
        PicoContainer container = scriptedContainerBuilderFactory.getContainerBuilder().buildContainer(null, null, true);

        if (nowait == false) {
            setShutdownHook(quiet, scriptedContainerBuilderFactory, container);
        } else {
//            shuttingDown(quiet, scriptedContainerBuilderFactory, containerRef);
        }
    }

    @SuppressWarnings("synthetic-access")
    private static void setShutdownHook(final boolean quiet, final ScriptedContainerBuilderFactory scriptedContainerBuilderFactory, final PicoContainer container) {
        // add a shutdown hook that will tell the builder to kill it.
        Runnable shutdownHook = new Runnable() {
            public void run() {
                shuttingDown(quiet, scriptedContainerBuilderFactory, container);
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
    }

    private static void shuttingDown(final boolean quiet, final ScriptedContainerBuilderFactory scriptedContainerBuilderFactory, final PicoContainer container) {
        try {
            scriptedContainerBuilderFactory.getContainerBuilder().killContainer(container);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (!quiet) {
                System.out.println("PicoContainer Standalone: Exiting Virtual Machine");
            }
        }
    }


    static CommandLine getCommandLine(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new PosixParser();
        return parser.parse(options, args);
    }

    private static void printUsage(Options options) {
        final String lineSeparator = System.getProperty("line.separator");

        final StringBuffer usage = new StringBuffer();
        usage.append(lineSeparator);
        usage.append("PicoContainer Standalone: -c <composition-file> [-q|-n|-h|-v]");
        usage.append(options.getOptions());
        System.out.println(usage.toString());
    }

    private static void printVersion() {
        System.out.println("1.1");
    }


}


