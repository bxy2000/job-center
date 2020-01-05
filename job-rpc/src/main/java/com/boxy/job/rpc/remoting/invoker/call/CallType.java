package com.boxy.job.rpc.remoting.invoker.call;

public enum CallType {


    SYNC,

    FUTURE,

    CALLBACK,

    ONEWAY;


    public static CallType match(String name, CallType defaultCallType){
        for (CallType item : CallType.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultCallType;
    }

}
