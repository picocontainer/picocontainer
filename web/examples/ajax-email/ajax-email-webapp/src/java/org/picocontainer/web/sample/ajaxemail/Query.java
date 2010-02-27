package org.picocontainer.web.sample.ajaxemail;


public interface Query {
    
    Object execute(Object arg);

    void declareImports(String imports);

    void declareParameters(String parameters);

}
