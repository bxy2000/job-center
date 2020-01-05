package com.boxy.registry.client.util.json;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.boxy.registry.client.util.json.BasicJson.*;

public class BasicJsonTest {
    @Test
    public void testAllMethod() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("arr", Arrays.asList("111", "222"));
        result.put("float", 1.11f);
        result.put("temp", null);

        String json = toJson(result);
        System.out.println(json);

        Map<String, Object> mapObj = parseMap(json);
        System.out.println(mapObj);

        List<Object> listInt = parseList("[111,222,33]");
        System.out.println(listInt);
    }
}