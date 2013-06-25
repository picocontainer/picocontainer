/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.security;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * CustomPermissionsURLClassLoader extends URLClassLoader, adding the ability to programatically add permissions easily.
 * To be effective for permission management, it should be run in conjunction with a policy that restricts
 * some of the classloaders, but not all.
 * It's not ordinarily used by PicoContainer, but is here because PicoContainer is common
 * to most classloader trees.
 *
 * @author Paul Hammant
 */
public class CustomPermissionsURLClassLoader extends URLClassLoader {
    private final Map<URL, Permissions> permissionsMap;

    public CustomPermissionsURLClassLoader(final URL[] urls, final Map<URL, Permissions> permissionsMap, final ClassLoader parent) {
        super(urls, parent);
        this.permissionsMap = permissionsMap;
    }

    @Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw decorateException(name, e);
        }
    }

    @Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            throw decorateException(name, e);
        }
    }

    private ClassNotFoundException decorateException(final String name, final ClassNotFoundException e) {
        if (name.startsWith("class ")) {
            return new ClassNotFoundException("Class '" + name + "' is not a classInstance.getName(). " +
                    "It's a classInstance.toString(). The clue is that it starts with 'class ', no classname contains a space.");
        }
        ClassLoader classLoader = this;
        StringBuffer sb = new StringBuffer("'").append(name).append("' classloader stack [");
        while (classLoader != null) {
            sb.append(classLoader.toString()).append("\n");
            final ClassLoader cl = classLoader;
            classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return cl.getParent();
                }
            });

        }
        return new ClassNotFoundException(sb.append("]").toString(), e);
    }

    @Override
	public String toString() {
        String result = CustomPermissionsURLClassLoader.class.getName() + " " + System.identityHashCode(this) + ":";
        URL[] urls = getURLs();
        for (URL url : urls) {
            result += "\n\t" + url.toString();
        }

        return result;
    }

    @Override
	public PermissionCollection getPermissions(final CodeSource codeSource) {
        return permissionsMap.get(codeSource.getLocation());
    }

}

