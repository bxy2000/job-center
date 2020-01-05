package com.boxy.job.rpc.registry.impl;

import com.boxy.job.rpc.registry.ServiceRegistry;
import com.boxy.registry.client.RegistryClient;
import com.boxy.registry.client.model.RegistryDataParam;

import java.util.*;

public class RegistryServiceRegistry extends ServiceRegistry {
    public static final String REGISTRY_ADDRESS = "REGISTRY_ADDRESS";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String BIZ = "BIZ";
    public static final String ENV = "ENV";

    private RegistryClient registryClient;

    public RegistryClient getRegistryClient() {
        return registryClient;
    }

    @Override
    public void start(Map<String, String> param) {
        String registryAddress = param.get(REGISTRY_ADDRESS);
        String accessToken = param.get(ACCESS_TOKEN);
        String biz = param.get(BIZ);
        String env = param.get(ENV);

        // fill
        biz = (biz != null && biz.trim().length() > 0) ? biz : "default";
        env = (env != null && env.trim().length() > 0) ? env : "default";

        registryClient = new RegistryClient(registryAddress, accessToken, biz, env);
    }

    @Override
    public void stop() {
        if (registryClient != null) {
            registryClient.stop();
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value) {
        if (keys == null || keys.size() == 0 || value == null) {
            return false;
        }

        // init
        List<RegistryDataParam> registryDataList = new ArrayList<>();
        for (String key : keys) {
            registryDataList.add(new RegistryDataParam(key, value));
        }

        return registryClient.registry(registryDataList);
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        if (keys == null || keys.size() == 0 || value == null) {
            return false;
        }

        // init
        List<RegistryDataParam> registryDataList = new ArrayList<>();
        for (String key : keys) {
            registryDataList.add(new RegistryDataParam(key, value));
        }

        return registryClient.remove(registryDataList);
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        return registryClient.discovery(keys);
    }

    @Override
    public TreeSet<String> discovery(String key) {
        return registryClient.discovery(key);
    }
}
