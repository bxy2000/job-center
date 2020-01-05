package com.boxy.job.rpc.remoting.invoker.route;

import com.boxy.job.rpc.remoting.invoker.route.impl.*;

public enum LoadBalance {
    RANDOM(new RpcLoadBalanceRandomStrategy()),
    ROUND(new RpcLoadBalanceRoundStrategy()),
    LRU(new RpcLoadBalanceLRUStrategy()),
    LFU(new RpcLoadBalanceLFUStrategy()),
    CONSISTENT_HASH(new RpcLoadBalanceConsistentHashStrategy());


    public final RpcLoadBalance rpcInvokerRouter;

    private LoadBalance(RpcLoadBalance rpcInvokerRouter) {
        this.rpcInvokerRouter = rpcInvokerRouter;
    }


    public static LoadBalance match(String name, LoadBalance defaultRouter) {
        for (LoadBalance item : LoadBalance.values()) {
            if (item.equals(name)) {
                return item;
            }
        }
        return defaultRouter;
    }



    /*public static void main(String[] args) {
        String serviceKey = "service";
        TreeSet<String> addressSet = new TreeSet<String>(){{
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
        }};

        for (LoadBalance item : LoadBalance.values()) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                String address = LoadBalance.LFU.rpcInvokerRouter.route(serviceKey, addressSet);
                //System.out.println(address);;
            }
            long end = System.currentTimeMillis();
            System.out.println(item.name() + " --- " + (end-start));
        }

    }*/


}