package com.boxy.job.rpc.util;

import java.util.concurrent.*;

public class ThreadPoolUtil {
    public static ThreadPoolExecutor makeServerThreadPool(final String serverType, int corePoolSize, int maxPoolSize){
        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "job-rpc, "+serverType+"-serverHandlerPool-" + r.hashCode());
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        throw new RpcException("job-rpc "+serverType+" Thread pool is EXHAUSTED!");
                    }
                });

        return serverHandlerPool;
    }
}
