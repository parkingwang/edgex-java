package net.nextabc.edgex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public class Codec {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static String toJSON(Object model) {
        return GSON.toJson(model);
    }

    public static <T> T fromJSON(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T fromJSON(byte[] json, Class<T> type) {
        return GSON.fromJson(new String(json), type);
    }
}
