package com.boxy.job.rpc.registry;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class Register {
    public abstract void start(Map<String, String> param);

    public abstract void stop();

    public abstract boolean registry(Set<String> keys, String value);

    public abstract boolean remove(Set<String> keys, String value);

    public abstract Map<String, TreeSet<String>> discovery(Set<String> keys);

    public abstract TreeSet<String> discovery(String key);
}
