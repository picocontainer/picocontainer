PicoContainer <-> Struts + PicoWebRemoting integration example.

To quickly run from command line type:

mvn jetty:run

Then open your browser window to http://localhost:8080/struts2-webapp-with-remoting/

Security Manager Example:

This example also containers a maven profile to run within an example security manager.  To execute the security manager example, type:

mvn -Prun-secure jetty:run-forked

