package com.picocontainer.web.sample.ajaxemail;

import com.picocontainer.web.StringFromCookie;

import com.picocontainer.injectors.ProviderAdapter;

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
