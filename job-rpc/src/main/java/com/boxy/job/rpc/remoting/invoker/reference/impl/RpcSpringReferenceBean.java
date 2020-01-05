package com.boxy.job.rpc.remoting.invoker.reference.impl;

import com.boxy.job.rpc.remoting.invoker.reference.RpcReferenceBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class RpcSpringReferenceBean implements FactoryBean<Object>, InitializingBean {


    // ---------------------- util ----------------------

    private RpcReferenceBean rpcReferenceBean;

    @Override
    public void afterPropertiesSet() {

        // init config
        this.rpcReferenceBean = new RpcReferenceBean();
    }


    @Override
    public Object getObject() throws Exception {
        return rpcReferenceBean.getObject();
    }

    @Override
    public Class<?> getObjectType() {
        return rpcReferenceBean.getIface();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
