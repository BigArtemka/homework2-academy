package org.example.app.util;

import jakarta.servlet.http.HttpServletRequest;
import org.example.framework.attribute.RequestAttributes;

import java.util.regex.Matcher;

public class AttrHelper {
    public AttrHelper() {
    }

    public static long getCardIdAttr(HttpServletRequest req) {
        return Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                .group("cardId"));
    }

    public static long getUserIdAttr(HttpServletRequest req) {
        return Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                .group("userId"));
    }
}
