package com.boxy.job.executor.utils;

import org.junit.Test;

import java.nio.charset.Charset;

public class TestEncoding {
    @Test
    public void testFileEncoding() throws Exception {
        System.out.println(System.getProperty("file.encoding"));
    }

    @Test
    public void testTrans() throws Exception {
        //获取系统默认编码
        System.out.println(System.getProperty("file.encoding"));

        //获取系统默认的字符编码
        System.out.println(Charset.defaultCharset());

        //获取系统默认语言

        System.out.println(System.getProperty("user.language"));

        //获取系统属性列表

        System.getProperties().list(System.out);
    }
}
