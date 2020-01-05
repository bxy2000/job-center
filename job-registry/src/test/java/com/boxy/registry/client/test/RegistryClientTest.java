package com.boxy.registry.client.test;

import com.boxy.registry.client.RegistryClient;
import com.boxy.registry.client.model.RegistryDataParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class RegistryClientTest {

    public static void main(String[] args) throws InterruptedException {
        RegistryClient registryClient = new RegistryClient("http://localhost:8080/registry-admin/", null, "job-rpc", "test");

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

        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
