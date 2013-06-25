package com.picocontainer.lifecycle;

public interface ThirdPartyStartable {

    void sstart() throws Exception;

    void sstop();

    void ddispose();
}
