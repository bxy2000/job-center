package com.boxy.registry.client.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter@Getter
public class RegistryParam {
    private String accessToken;
    private String biz;
    private String env;

    private List<RegistryDataParam> registryDataList;
    private List<String> keys;
}
