package com.boxy.job.core.executor.impl;

import com.boxy.job.core.executor.JobExecutor;
import com.boxy.job.core.handler.annotation.Job;
import com.boxy.job.core.handler.impl.MethodJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * xxl-job executor (for frameless)
 *
 * @author xuxueli 2020-11-05
 */
public class JobSimpleExecutor extends JobExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JobSimpleExecutor.class);


    private List<Object> xxlJobBeanList = new ArrayList<>();
    public List<Object> getXxlJobBeanList() {
        return xxlJobBeanList;
    }
    public void setXxlJobBeanList(List<Object> xxlJobBeanList) {
        this.xxlJobBeanList = xxlJobBeanList;
    }


    public void start() {

        // init JobHandler Repository (for method)
        initJobHandlerMethodRepository(xxlJobBeanList);

        // super start
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        super.destroy();
    }


    private void initJobHandlerMethodRepository(List<Object> xxlJobBeanList) {
        if (xxlJobBeanList==null || xxlJobBeanList.size()==0) {
            return;
        }

        // init job handler from method
        for (Object bean: xxlJobBeanList) {
            // method
            Method[] methods = bean.getClass().getDeclaredMethods();
            if (methods==null || methods.length==0) {
                continue;
            }
            for (Method executeMethod : methods) {

                // anno
                Job job = executeMethod.getAnnotation(Job.class);
                if (job == null) {
                    continue;
                }

                String name = job.value();
                if (name.trim().length() == 0) {
                    throw new RuntimeException("xxl-job method-jobhandler name invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
                }
                if (loadJobHandler(name) != null) {
                    throw new RuntimeException("xxl-job jobhandler[" + name + "] naming conflicts.");
                }

                // execute method
                /*if (!(method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(String.class))) {
                    throw new RuntimeException("xxl-job method-jobhandler param-classtype invalid, for[" + bean.getClass() + "#" + method.getName() + "] , " +
                            "The correct method format like \" public ReturnT<String> execute(String param) \" .");
                }
                if (!method.getReturnType().isAssignableFrom(ReturnT.class)) {
                    throw new RuntimeException("xxl-job method-jobhandler return-classtype invalid, for[" + bean.getClass() + "#" + method.getName() + "] , " +
                            "The correct method format like \" public ReturnT<String> execute(String param) \" .");
                }*/

                executeMethod.setAccessible(true);

                // init and destory
                Method initMethod = null;
                Method destroyMethod = null;

                if (job.init().trim().length() > 0) {
                    try {
                        initMethod = bean.getClass().getDeclaredMethod(job.init());
                        initMethod.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("xxl-job method-jobhandler initMethod invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
                    }
                }
                if (job.destroy().trim().length() > 0) {
                    try {
                        destroyMethod = bean.getClass().getDeclaredMethod(job.destroy());
                        destroyMethod.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("xxl-job method-jobhandler destroyMethod invalid, for[" + bean.getClass() + "#" + executeMethod.getName() + "] .");
                    }
                }

                // registry jobhandler
                registJobHandler(name, new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod));


            }

        }

    }

}
