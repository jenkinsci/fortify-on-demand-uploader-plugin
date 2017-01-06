package org.jenkinsci.plugins.fodupload;

public class Utils {

    public static int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
}
