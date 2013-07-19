package com.picocontainer.web.sample.webwork1;

import javax.servlet.http.HttpServletRequest;

public class Brand {
    private final String name;

    public Brand(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static class FromRequest extends Brand {

        public FromRequest(final HttpServletRequest req) {
            super(fromRequest(req));
        }

        private static String fromRequest(final HttpServletRequest req) {
            String name = req.getRemoteHost().toUpperCase();
            if (name == null) {
                name = "";
            } else if ("127.0.0.1".equals(name)) {
                name = "testing-brand";
            }
            return name;
        }

    }

}