package com.wallet.account.infrastructure.adapters.in.web;

import jakarta.servlet.http.HttpServletRequest;

public class HttpUtils {

    public static <T> T getAttributeFromRequest(HttpServletRequest request, String attributeName, T defaultValue) {
        Object attribute = request.getAttribute(attributeName);
        if (defaultValue.getClass().isInstance(attribute)) {
            return (T) attribute;
        }
        return defaultValue;
    }
}
