package com.boxy.job.rpc.test;

import com.boxy.job.rpc.serialize.Serializer;
import com.boxy.job.rpc.serialize.impl.HessianSerializer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SerializerTest {

    @Test
    public void testAll() throws IllegalAccessException, InstantiationException {
        Serializer serializer = HessianSerializer.class.newInstance();
        System.out.println(serializer);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("aaa", "111");
            map.put("bbb", "222");
            System.out.println(serializer.deserialize(serializer.serialize("ddddddd"), String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
