package org.picocontainer.web.sample.ajaxemail;

import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.web.StringFromCookie;

import javax.servlet.http.HttpServletRequest;

public class CookieUserProviderAdapter extends ProviderAdapter {

    public User provide(UserStore userStore, HttpServletRequest req) {
        try {
            String name = new StringFromCookie("userName").provide(req);
            if (name.trim().length() == 0) {
                throw new UserNotLoggedIn();
            }
            return userStore.getUser(name);
        } catch (StringFromCookie.CookieNotFound e) {
            throw new UserNotLoggedIn();
        }
    }

    @SuppressWarnings("serial")
	public static class UserNotLoggedIn extends AjaxEmailException {
		UserNotLoggedIn() {
			super("not logged in");
		}
	}

}
