package com.boxy.job.rpc.remoting.invoker.impl;

import com.boxy.job.rpc.registry.ServiceRegistry;
import com.boxy.job.rpc.remoting.invoker.RpcInvokerFactory;
import com.boxy.job.rpc.remoting.invoker.annotation.RpcReference;
import com.boxy.job.rpc.remoting.invoker.reference.RpcReferenceBean;
import com.boxy.job.rpc.remoting.provider.RpcProviderFactory;
import com.boxy.job.rpc.util.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RpcSpringInvokerFactory extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean,DisposableBean, BeanFactoryAware {
    private Logger logger = LoggerFactory.getLogger(RpcSpringInvokerFactory.class);

    // ---------------------- config ----------------------
    private Class<? extends ServiceRegistry> serviceRegistryClass;          // class.forname
    private Map<String, String> serviceRegistryParam;


    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }


    // ---------------------- util ----------------------

    private RpcInvokerFactory rpcInvokerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        // start invoker factory
        rpcInvokerFactory = new RpcInvokerFactory(serviceRegistryClass, serviceRegistryParam);
        rpcInvokerFactory.start();
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {

        // collection
        final Set<String> serviceKeyList = new HashSet<>();

        // parse RpcReferenceBean
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(RpcReference.class)) {
                    // valid
                    Class iface = field.getType();
                    if (!iface.isInterface()) {
                        throw new RpcException("job-rpc, reference(RpcReference) must be interface.");
                    }

                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);

                    // init reference bean
                    RpcReferenceBean referenceBean = new RpcReferenceBean();
                    referenceBean.setClient(rpcReference.client());
                    referenceBean.setSerializer(rpcReference.serializer());
                    referenceBean.setCallType(rpcReference.callType());
                    referenceBean.setLoadBalance(rpcReference.loadBalance());
                    referenceBean.setIface(iface);
                    referenceBean.setVersion(rpcReference.version());
                    referenceBean.setTimeout(rpcReference.timeout());
                    referenceBean.setAddress(rpcReference.address());
                    referenceBean.setAccessToken(rpcReference.accessToken());
                    referenceBean.setInvokeCallback(null);
                    referenceBean.setInvokerFactory(rpcInvokerFactory);


                    // get proxyObj
                    Object serviceProxy = null;
                    try {
                        serviceProxy = referenceBean.getObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    // set bean
                    field.setAccessible(true);
                    field.set(bean, serviceProxy);

                    logger.info(" job-rpc, invoker factory init reference bean success. serviceKey = {}, bean.field = {}.{}",
                            RpcProviderFactory.makeServiceKey(iface.getName(), rpcReference.version()), beanName, field.getName());

                    // collection
                    String serviceKey = RpcProviderFactory.makeServiceKey(iface.getName(), rpcReference.version());
                    serviceKeyList.add(serviceKey);

                }
            }
        });

        // mult discovery
        if (rpcInvokerFactory.getServiceRegistry() != null) {
            try {
                rpcInvokerFactory.getServiceRegistry().discovery(serviceKeyList);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return super.postProcessAfterInstantiation(bean, beanName);
    }


    @Override
    public void destroy() throws Exception {

        // stop invoker factory
        rpcInvokerFactory.stop();
    }

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
