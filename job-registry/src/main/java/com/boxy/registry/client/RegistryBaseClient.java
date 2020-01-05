package com.boxy.registry.client;

import com.boxy.registry.client.util.BasicHttpUtil;
import com.boxy.registry.client.model.RegistryDataParam;
import com.boxy.registry.client.model.RegistryParam;
import com.boxy.registry.client.util.json.BasicJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RegistryBaseClient {
    private static Logger logger = LoggerFactory.getLogger(RegistryBaseClient.class);

    private String adminAddress;
    private String accessToken;
    private String biz;
    private String env;

    private List<String> adminAddressArr;


    public RegistryBaseClient(String adminAddress, String accessToken, String biz, String env) {
        this.adminAddress = adminAddress;
        this.accessToken = accessToken;
        this.biz = biz;
        this.env = env;

        // valid
        if (adminAddress==null || adminAddress.trim().length()==0) {
            throw new RuntimeException("job-registry adminAddress empty");
        }
        if (biz==null || biz.trim().length()<4 || biz.trim().length()>255) {
            throw new RuntimeException("job-registry biz empty Invalid[4~255]");
        }
        if (env==null || env.trim().length()<2 || env.trim().length()>255) {
            throw new RuntimeException("job-registry biz env Invalid[2~255]");
        }

        // parse
        adminAddressArr = new ArrayList<>();
        if (adminAddress.contains(",")) {
            adminAddressArr.addAll(Arrays.asList(adminAddress.split(",")));
        } else {
            adminAddressArr.add(adminAddress);
        }
    }

    public boolean registry(List<RegistryDataParam> registryDataList){
        // valid
        if (registryDataList==null || registryDataList.size()==0) {
            throw new RuntimeException("job-registry registryDataList empty");
        }
        for (RegistryDataParam registryParam: registryDataList) {
            if (registryParam.getKey()==null || registryParam.getKey().trim().length()<4 || registryParam.getKey().trim().length()>255) {
                throw new RuntimeException("job-registry registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue()==null || registryParam.getValue().trim().length()<4 || registryParam.getValue().trim().length()>255) {
                throw new RuntimeException("job-registry registryDataList#value Invalid[4~255]");
            }
        }

        // pathUrl
        String pathUrl = "/api/registry";

        // param
        RegistryParam registryParam = new RegistryParam();
        registryParam.setAccessToken(this.accessToken);
        registryParam.setBiz(this.biz);
        registryParam.setEnv(this.env);
        registryParam.setRegistryDataList(registryDataList);

        String paramsJson = BasicJson.toJson(registryParam);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, 5);
        return respObj!=null?true:false;
    }

    private Map<String, Object> requestAndValid(String pathUrl, String requestBody, int timeout){

        for (String adminAddressUrl: adminAddressArr) {
            String finalUrl = adminAddressUrl + pathUrl;

            // request
            String responseData = BasicHttpUtil.postBody(finalUrl, requestBody, timeout);
            if (responseData == null) {
                return null;
            }

            // parse resopnse
            Map<String, Object> resopnseMap = null;
            try {
                resopnseMap = BasicJson.parseMap(responseData);
            } catch (Exception e) { }


            // valid resopnse
            if (resopnseMap==null
                    || !resopnseMap.containsKey("code")
                    || !"200".equals(String.valueOf(resopnseMap.get("code")))
                    ) {
                logger.warn("RegistryBaseClient response fail, responseData={}", responseData);
                return null;
            }

            return resopnseMap;
        }


        return null;
    }

    public boolean remove(List<RegistryDataParam> registryDataList) {
        // valid
        if (registryDataList==null || registryDataList.size()==0) {
            throw new RuntimeException("job-registry registryDataList empty");
        }
        for (RegistryDataParam registryParam: registryDataList) {
            if (registryParam.getKey()==null || registryParam.getKey().trim().length()<4 || registryParam.getKey().trim().length()>255) {
                throw new RuntimeException("job-registry registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue()==null || registryParam.getValue().trim().length()<4 || registryParam.getValue().trim().length()>255) {
                throw new RuntimeException("job-registry registryDataList#value Invalid[4~255]");
            }
        }

        // pathUrl
        String pathUrl = "/api/remove";

        // param
        RegistryParam registryParam = new RegistryParam();
        registryParam.setAccessToken(this.accessToken);
        registryParam.setBiz(this.biz);
        registryParam.setEnv(this.env);
        registryParam.setRegistryDataList(registryDataList);

        String paramsJson = BasicJson.toJson(registryParam);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, 5);
        return respObj!=null?true:false;
    }

    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        // valid
        if (keys==null || keys.size()==0) {
            throw new RuntimeException("job-registry keys empty");
        }

        // pathUrl
        String pathUrl = "/api/discovery";

        // param
        RegistryParam registryParam = new RegistryParam();
        registryParam.setAccessToken(this.accessToken);
        registryParam.setBiz(this.biz);
        registryParam.setEnv(this.env);
        registryParam.setKeys(new ArrayList<String>(keys));

        String paramsJson = BasicJson.toJson(registryParam);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, 5);

        // parse
        if (respObj!=null && respObj.containsKey("data")) {
            Map<String, TreeSet<String>> data = (Map<String, TreeSet<String>>) respObj.get("data");
            return data;
        }

        return null;
    }

    public boolean monitor(Set<String> keys) {
        // valid
        if (keys==null || keys.size()==0) {
            throw new RuntimeException("job-registry keys empty");
        }

        // pathUrl
        String pathUrl = "/api/monitor";

        // param
        RegistryParam registryParam = new RegistryParam();
        registryParam.setAccessToken(this.accessToken);
        registryParam.setBiz(this.biz);
        registryParam.setEnv(this.env);
        registryParam.setKeys(new ArrayList<String>(keys));

        String paramsJson = BasicJson.toJson(registryParam);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, 60);
        return respObj!=null?true:false;
    }
}
