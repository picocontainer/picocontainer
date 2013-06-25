package com.picocontainer.web.sample.ajaxemail;

import com.picocontainer.web.remoting.NullPicoWebRemotingMonitor;

public class AjaxEmailWebRemotingMonitor extends NullPicoWebRemotingMonitor {

    protected Class<? extends RuntimeException> getAppBaseRuntimeException() {
        return AjaxEmailException.class;
    }
}
