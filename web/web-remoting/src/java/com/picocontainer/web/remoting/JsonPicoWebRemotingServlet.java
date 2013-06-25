/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.remoting;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.WriterWrapper;
import com.thoughtworks.xstream.io.json.JsonWriter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

/**
 * Servlet that uses JSON as the form of the reply.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class JsonPicoWebRemotingServlet extends AbstractPicoWebRemotingServlet  {

	@Override
	protected XStream createXStream() {
		return new XStream(makeJsonDriver(JsonWriter.DROP_ROOT_MODE));
	}

    public static HierarchicalStreamDriver makeJsonDriver(final int dropRootMode) {
        HierarchicalStreamDriver driver = new HierarchicalStreamDriver() {
            public HierarchicalStreamReader createReader(Reader reader) {
                throw new UnsupportedOperationException();
            }

            public HierarchicalStreamReader createReader(InputStream inputStream) {
                throw new UnsupportedOperationException();
            }

            public HierarchicalStreamReader createReader(File file) {
                throw new UnsupportedOperationException();
            }

            public HierarchicalStreamReader createReader(URL url) {
                throw new UnsupportedOperationException();
            }

            public HierarchicalStreamWriter createWriter(Writer out) {
                HierarchicalStreamWriter jsonWriter = new JsonWriter(out, dropRootMode);
                return new WriterWrapper(jsonWriter) {
                    public void startNode(String name) {
                        startNode(name, null);
                    }

                    @SuppressWarnings({ "rawtypes" })
					public void startNode(String name, Class clazz) {
                        ((JsonWriter) wrapped).startNode(name.replace('-', '_'), clazz);
                    }
                };
            }

            public HierarchicalStreamWriter createWriter(OutputStream outputStream) {
                throw new UnsupportedOperationException();
            }
        };
        return driver;
    }


}
