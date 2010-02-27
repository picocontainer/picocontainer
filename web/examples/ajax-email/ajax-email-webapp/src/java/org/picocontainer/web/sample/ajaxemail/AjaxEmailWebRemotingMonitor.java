package org.picocontainer.web.sample.ajaxemail;

import org.picocontainer.web.remoting.NullPicoWebRemotingMonitor;

public class AjaxEmailWebRemotingMonitor extends NullPicoWebRemotingMonitor {

    protected Class<? extends RuntimeException> getAppBaseRuntimeException() {
        return AjaxEmailException.class;
    }
}
