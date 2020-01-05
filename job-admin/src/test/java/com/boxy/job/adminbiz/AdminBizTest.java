package com.boxy.job.adminbiz;

import com.boxy.job.core.biz.AdminBiz;
import com.boxy.job.core.biz.client.AdminBizClient;
import com.boxy.job.core.biz.model.HandleCallbackParam;
import com.boxy.job.core.biz.model.RegistryParam;
import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.enums.RegistryConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AdminBizTest {

    // admin-client
    private static String addressUrl = "http://127.0.0.1:8080/job-admin/";
    private static String accessToken = null;


    @Test
    public void callback() throws Exception {
        AdminBiz adminBiz = new AdminBizClient(addressUrl, accessToken);

        HandleCallbackParam param = new HandleCallbackParam();
        param.setLogId(1);
        param.setExecuteResult(ReturnT.SUCCESS);

        List<HandleCallbackParam> callbackParamList = Arrays.asList(param);

        ReturnT<String> returnT = adminBiz.callback(callbackParamList);

        Assert.assertTrue(returnT.getCode() == ReturnT.SUCCESS_CODE);
    }

    /**
     * registry executor
     *
     * @throws Exception
     */
    @Test
    public void registry() throws Exception {
        AdminBiz adminBiz = new AdminBizClient(addressUrl, accessToken);

        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), "job-executor-example", "127.0.0.1:9999");
        ReturnT<String> returnT = adminBiz.registry(registryParam);

        Assert.assertTrue(returnT.getCode() == ReturnT.SUCCESS_CODE);
    }

    /**
     * registry executor remove
     *
     * @throws Exception
     */
    @Test
    public void registryRemove() throws Exception {
        AdminBiz adminBiz = new AdminBizClient(addressUrl, accessToken);

        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), "job-executor-example", "127.0.0.1:9999");
        ReturnT<String> returnT = adminBiz.registryRemove(registryParam);

        Assert.assertTrue(returnT.getCode() == ReturnT.SUCCESS_CODE);

    }

}
