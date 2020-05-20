package com.boxy.registry.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor@AllArgsConstructor
public class RegistryParam {
    private String accessToken;
    private String biz;
    private String env;

    private List<RegistryDataParam> registryDataList;
    private List<String> keys;
}
