package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public class Env {

    public static String getString(String key, String defaultValue) {
        final String value = System.getenv(key);
        if (null != value) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public static int getInt(String key, int defaultValue) {
        final String value = System.getenv(key);
        if (null != value) {
            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        final String value = System.getenv(key);
        if (null != value) {
            return Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }

}
