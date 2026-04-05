package com.example.onlinebookshop.util;

import java.util.regex.Pattern;

/**
 * Normalize and validate Vietnam mobile numbers (10 digits, 03x / 05x / 07x / 08x / 09x).
 */
public final class VietnamPhoneUtils {

    private static final Pattern VN_MOBILE = Pattern.compile("^0[35789]\\d{8}$");

    private VietnamPhoneUtils() {
    }

    public static String normalizeVnPhone(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().replaceAll("[\\s.\\-()]", "");
        if (s.startsWith("+84")) {
            return "0" + s.substring(3);
        }
        if (s.startsWith("84") && s.length() >= 10) {
            return "0" + s.substring(2);
        }
        return s;
    }

    /** Non-empty string that normalizes to a valid VN mobile. */
    public static boolean isValidVnPhone(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String n = normalizeVnPhone(raw);
        return VN_MOBILE.matcher(n).matches();
    }
}
