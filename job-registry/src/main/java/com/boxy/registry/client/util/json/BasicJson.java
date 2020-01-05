package com.boxy.registry.client.util.json;

import java.util.List;
import java.util.Map;

public class BasicJson {
    private static final BasicJsonReader basicJsonReader = new BasicJsonReader();
    private static final BasicJsonWriter basicJsonwriter = new BasicJsonWriter();

    public static String toJson(Object object) {
        return basicJsonwriter.toJson(object);
    }

    public static Map<String, Object> parseMap(String json) {
        return basicJsonReader.parseMap(json);
    }

    public static List<Object> parseList(String json) {
        return basicJsonReader.parseList(json);
    }
}
