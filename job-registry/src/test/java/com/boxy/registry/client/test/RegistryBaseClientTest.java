package com.boxy.registry.client.test;

import com.boxy.registry.client.RegistryBaseClient;
import com.boxy.registry.client.model.RegistryDataParam;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class RegistryBaseClientTest {

    @Test
    public void testAll() throws InterruptedException {
        RegistryBaseClient registryClient = new RegistryBaseClient("http://localhost:8080/registry-admin/", null, "job-rpc", "dev");

        // registry test
        List<RegistryDataParam> registryDataList = new ArrayList<>();
        registryDataList.add(new RegistryDataParam("service01", "address01"));
        registryDataList.add(new RegistryDataParam("service02", "address02"));
        System.out.println("registry:" + registryClient.registry(registryDataList));
        TimeUnit.SECONDS.sleep(2);

        // discovery test
        Set<String> keys = new TreeSet<>();
        keys.add("service01");
        keys.add("service02");
        System.out.println("discovery:" + registryClient.discovery(keys));


        // remove test
        System.out.println("remove:" + registryClient.remove(registryDataList));
        TimeUnit.SECONDS.sleep(2);

        // discovery test
        System.out.println("discovery:" + registryClient.discovery(keys));

        // monitor test
        TimeUnit.SECONDS.sleep(10);
        System.out.println("monitor...");
        registryClient.monitor(keys);
    }

}
