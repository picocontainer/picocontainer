package org.picocontainer.web.sample.struts2.pwr;

import javax.servlet.http.HttpServletRequest;

public class Brand {
    private String name;

    public Brand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static class FromRequest extends Brand {

        public FromRequest(HttpServletRequest req) {
            super(fromRequest(req));
        }

        private static String fromRequest(HttpServletRequest req) {
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