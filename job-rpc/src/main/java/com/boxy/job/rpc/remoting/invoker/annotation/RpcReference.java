package com.boxy.job.rpc.remoting.invoker.annotation;

import com.boxy.job.rpc.remoting.invoker.call.CallType;
import com.boxy.job.rpc.remoting.invoker.route.LoadBalance;
import com.boxy.job.rpc.remoting.net.Client;
import com.boxy.job.rpc.remoting.net.impl.netty.client.NettyClient;
import com.boxy.job.rpc.serialize.Serializer;
import com.boxy.job.rpc.serialize.impl.HessianSerializer;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcReference {
    Class<? extends Client> client() default NettyClient.class;
    Class<? extends Serializer> serializer() default HessianSerializer.class;
    CallType callType() default CallType.SYNC;
    LoadBalance loadBalance() default LoadBalance.ROUND;
    String version() default "";
    long timeout() default 1000;
    String address() default "";
    String accessToken() default "";
}
