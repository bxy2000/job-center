package com.boxy.job.admin.controller;

import com.boxy.job.admin.controller.annotation.PermissionLimit;
import com.boxy.job.admin.core.conf.JobAdminConfig;
import com.boxy.job.admin.core.exception.JobException;
import com.boxy.job.admin.core.util.JacksonUtil;
import com.boxy.job.core.biz.AdminBiz;
import com.boxy.job.core.biz.model.HandleCallbackParam;
import com.boxy.job.core.biz.model.RegistryParam;
import com.boxy.job.core.biz.model.ReturnT;
import com.boxy.job.core.util.JobRemotingUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api")
public class JobApiController {

    @Resource
    private AdminBiz adminBiz;


    // ---------------------- base ----------------------

    /**
     * valid access token
     */
    private void validAccessToken(HttpServletRequest request){
        if (JobAdminConfig.getAdminConfig().getAccessToken()!=null
                && JobAdminConfig.getAdminConfig().getAccessToken().trim().length()>0
                && !JobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(JobRemotingUtil.RPC_ACCESS_TOKEN))) {
            throw new JobException("The access token is wrong.");
        }
    }

    /**
     * parse Param
     */
    private Object parseParam(String data, Class<?> parametrized, Class<?>... parameterClasses){
        Object param = null;
        try {
            if (parameterClasses != null) {
                param = JacksonUtil.readValue(data, parametrized, parameterClasses);
            } else {
                param = JacksonUtil.readValue(data, parametrized);
            }
        } catch (Exception e) { }
        if (param==null) {
            throw new JobException("The request data invalid.");
        }
        return param;
    }

    // ---------------------- admin biz ----------------------

    /**
     * callback
     *
     * @param data
     * @return
     */
    @RequestMapping("/callback")
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> callback(HttpServletRequest request, @RequestBody(required = false) String data) {
        // valid
        validAccessToken(request);

        // param
        List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) parseParam(data, List.class, HandleCallbackParam.class);

        // invoke
        return adminBiz.callback(callbackParamList);
    }



    /**
     * registry
     *
     * @param data
     * @return
     */
    @RequestMapping("/registry")
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> registry(HttpServletRequest request, @RequestBody(required = false) String data) {
        // valid
        validAccessToken(request);

        // param
        RegistryParam registryParam = (RegistryParam) parseParam(data, RegistryParam.class);

        // invoke
        return adminBiz.registry(registryParam);
    }

    /**
     * registry remove
     *
     * @param data
     * @return
     */
    @RequestMapping("/registryRemove")
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> registryRemove(HttpServletRequest request, @RequestBody(required = false) String data) {
        // valid
        validAccessToken(request);

        // param
        RegistryParam registryParam = (RegistryParam) parseParam(data, RegistryParam.class);

        // invoke
        return adminBiz.registryRemove(registryParam);
    }

    // ---------------------- job biz ----------------------

}
