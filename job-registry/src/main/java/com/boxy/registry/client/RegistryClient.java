package com.boxy.registry.client;

import com.boxy.registry.client.util.json.BasicJson;
import com.boxy.registry.client.model.RegistryDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class RegistryClient {
    private static Logger logger = LoggerFactory.getLogger(RegistryClient.class);


    private volatile Set<RegistryDataParam> registryData = new HashSet<>();
    private volatile ConcurrentMap<String, TreeSet<String>> discoveryData = new ConcurrentHashMap<>();

    private Thread registryThread;
    private Thread discoveryThread;
    private volatile boolean registryThreadStop = false;


    private RegistryBaseClient registryBaseClient;

    public RegistryClient(String adminAddress, String accessToken, String biz, String env) {
        registryBaseClient = new RegistryBaseClient(adminAddress, accessToken, biz, env);
        logger.info("job-registry, RegistryClient init .... [adminAddress={}, accessToken={}, biz={}, env={}]", adminAddress, accessToken, biz, env);

        // registry thread
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!registryThreadStop) {
                    try {
                        if (registryData.size() > 0) {
                            boolean ret = registryBaseClient.registry(new ArrayList<RegistryDataParam>(registryData));
                            logger.debug("job-registry, refresh registry data {}, registryData = {}", ret ? "success" : "fail", registryData);
                        }
                    } catch (Exception e) {
                        if (!registryThreadStop) {
                            logger.error("job-registry, registryThread error.", e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        if (!registryThreadStop) {
                            logger.error("job-registry, registryThread error.", e);
                        }
                    }
                }
                logger.info("job-registry, registryThread stoped.");
            }
        });
        registryThread.setName("job-registry, RegistryClient registryThread.");
        registryThread.setDaemon(true);
        registryThread.start();

        // discovery thread
        discoveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!registryThreadStop) {

                    if (discoveryData.size() == 0) {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (Exception e) {
                            if (!registryThreadStop) {
                                logger.error("job-registry, discoveryThread error.", e);
                            }
                        }
                    } else {
                        try {
                            // monitor
                            boolean monitorRet = registryBaseClient.monitor(discoveryData.keySet());

                            // avoid fail-retry request too quick
                            if (!monitorRet) {
                                TimeUnit.SECONDS.sleep(10);
                            }

                            // refreshDiscoveryData, all
                            refreshDiscoveryData(discoveryData.keySet());
                        } catch (Exception e) {
                            if (!registryThreadStop) {
                                logger.error("job-registry, discoveryThread error.", e);
                            }
                        }
                    }

                }
                logger.info("job-registry, discoveryThread stoped.");
            }
        });
        discoveryThread.setName("job-registry, RegistryClient discoveryThread.");
        discoveryThread.setDaemon(true);
        discoveryThread.start();

        logger.info("job-registry, RegistryClient init success.");
    }


    public void stop() {
        registryThreadStop = true;
        if (registryThread != null) {
            registryThread.interrupt();
        }
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
    }

    public boolean registry(List<RegistryDataParam> registryDataList) {

        // valid
        RegistryBaseClient.checkRegistryDataList(registryDataList);

        // cache
        registryData.addAll(registryDataList);

        // remote
        registryBaseClient.registry(registryDataList);

        return true;
    }

    public boolean remove(List<RegistryDataParam> registryDataList) {
        // valid
        RegistryBaseClient.checkRegistryDataList(registryDataList);

        // cache
        registryData.removeAll(registryDataList);

        // remote
        registryBaseClient.remove(registryDataList);

        return true;
    }

    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        if (keys == null || keys.size() == 0) {
            return null;
        }

        // find from local
        Map<String, TreeSet<String>> registryDataTmp = new HashMap<String, TreeSet<String>>();
        for (String key : keys) {
            TreeSet<String> valueSet = discoveryData.get(key);
            if (valueSet != null) {
                registryDataTmp.put(key, valueSet);
            }
        }

        // not find all, find from remote
        if (keys.size() != registryDataTmp.size()) {

            // refreshDiscoveryData, some, first use
            refreshDiscoveryData(keys);

            // find from local
            for (String key : keys) {
                TreeSet<String> valueSet = discoveryData.get(key);
                if (valueSet != null) {
                    registryDataTmp.put(key, valueSet);
                }
            }

        }

        return registryDataTmp;
    }

    /**
     * refreshDiscoveryData, some or all
     */
    private void refreshDiscoveryData(Set<String> keys) {
        if (keys == null || keys.size() == 0) {
            return;
        }

        // discovery mult
        Map<String, TreeSet<String>> updatedData = new HashMap<>();

        Map<String, TreeSet<String>> keyValueListData = registryBaseClient.discovery(keys);
        if (keyValueListData != null) {
            for (String keyItem : keyValueListData.keySet()) {

                // list > set
                TreeSet<String> valueSet = new TreeSet<>();
                valueSet.addAll(keyValueListData.get(keyItem));

                // valid if updated
                boolean updated = true;
                TreeSet<String> oldValSet = discoveryData.get(keyItem);
                if (oldValSet != null && BasicJson.toJson(oldValSet).equals(BasicJson.toJson(valueSet))) {
                    updated = false;
                }

                // set
                if (updated) {
                    discoveryData.put(keyItem, valueSet);
                    updatedData.put(keyItem, valueSet);
                }

            }
        }

        if (updatedData.size() > 0) {
            logger.info("job-registry, refresh discovery data finish, discoveryData(updated) = {}", updatedData);
        }
        logger.debug("job-registry, refresh discovery data finish, discoveryData = {}", discoveryData);
    }


    public TreeSet<String> discovery(String key) {
        if (key == null) {
            return null;
        }

        Map<String, TreeSet<String>> keyValueSetTmp = discovery(new HashSet<String>(Arrays.asList(key)));
        if (keyValueSetTmp != null) {
            return keyValueSetTmp.get(key);
        }
        return null;
    }
}
