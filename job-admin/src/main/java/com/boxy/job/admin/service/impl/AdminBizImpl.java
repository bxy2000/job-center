package com.boxy.job.admin.service.impl;

import com.boxy.job.admin.core.thread.JobCompleteHelper;
import com.boxy.job.admin.core.thread.JobRegistryHelper;
import com.boxy.job.core.biz.AdminBiz;
import com.boxy.job.core.biz.model.HandleCallbackParam;
import com.boxy.job.core.biz.model.RegistryParam;
import com.boxy.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminBizImpl implements AdminBiz {
    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return JobCompleteHelper.getInstance().callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registryRemove(registryParam);
    }
}
