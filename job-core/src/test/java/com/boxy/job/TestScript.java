package com.boxy.job;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestScript {
    @Test
    public void test() {
        new A().a();
    }
}

class A {
    public void a() {
        System.out.println(b());
        System.out.println(b("1"));
        System.out.println(b("1", "2"));
    }

    public String[] b(String... params) {
        List<String> result = new ArrayList<>();
        for (String param : params) {
            result.add(param);
        }

        return result.toArray(new String[result.size()]);
    }
}
