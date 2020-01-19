package com.boxy.job.rpc.test;

import com.boxy.job.rpc.serialize.Serializer;
import com.boxy.job.rpc.serialize.impl.HessianSerializer;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SerializerTest {
    Serializer serializer = HessianSerializer.class.newInstance();

    public SerializerTest() throws IllegalAccessException, InstantiationException {
    }

    @Test
    public void testString() {
        System.out.println(serializer.deserialize(serializer.serialize("ddddddd"), String.class));
    }

    @Test
    public void testMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("aaa", "111");
        map.put("bbb", "222");

        System.out.println(serializer.deserialize(serializer.serialize(map), map.getClass()));
    }

    @Test
    public void testObject() {
        Person p = new Person(12L, "zhangsan", "nan", new Date());

        System.out.println(serializer.deserialize(serializer.serialize(p), Person.class));
    }
}

class Person implements Serializable {
    private Long id;
    private String name;
    private String gender;
    private Date birthday;

    public Person(Long id, String name, String gender, Date birthday) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}