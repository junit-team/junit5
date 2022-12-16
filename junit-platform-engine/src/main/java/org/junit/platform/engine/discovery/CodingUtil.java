package org.junit.platform.engine.discovery;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

final class CodingUtil {
    private CodingUtil() {
    }

    static String urlDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw  new IllegalStateException("UTF-8 is not supported", e);
        }
    }

    static String urlEncode(String plain) {
        try {
            return URLEncoder.encode(plain, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw  new IllegalStateException("UTF-8 is not supported", e);
        }
    }

    static String normalizeDirectorySeparators(String path) {
        return path.replace('\\', '/');
    }
}
