package com.boxy.job.rpc.test;

import com.boxy.job.rpc.util.IpUtil;
import org.junit.Test;

import java.net.UnknownHostException;

public class IpUtilTest {

    @Test
    public void testAll() throws UnknownHostException {
        System.out.println(IpUtil.getIp());
        System.out.println(IpUtil.getIpPort(8080));
    }
}
