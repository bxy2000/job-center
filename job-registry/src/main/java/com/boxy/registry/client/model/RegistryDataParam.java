package com.boxy.registry.client.model;

import lombok.*;

@Setter@Getter
@NoArgsConstructor@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistryDataParam {
    private String key;
    private String value;
}
