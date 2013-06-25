package com.picocontainer.web;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.picocontainer.web.SessionStoreHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class PrintSessionSizeDetailsForDebugging {

    public static void printItIfDebug(boolean debug, SessionStoreHolder ssh) throws IOException {
        if ( debug ){
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               ObjectOutputStream oos = new ObjectOutputStream(baos);
               oos.writeObject(ssh);
               oos.close();
               baos.close();
               String xml = new XStream(new PureJavaReflectionProvider()).toXML(ssh);
               int bytes = baos.toByteArray().length;
               System.out.println("** Session written (" + bytes + " bytes), xml representation= " + xml);
        }
    }

}
