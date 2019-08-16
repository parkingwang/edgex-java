package net.nextabc.edgex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public class Codec {

    public static final Type TYPE_KV_MAP = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();

    public static String toJSON(Object model) {
        return GSON.toJson(model);
    }

    public static <T> T fromJSON(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T fromJSON(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T fromJSON(byte[] json, Class<T> type) {
        return GSON.fromJson(new String(json), type);
    }
}
