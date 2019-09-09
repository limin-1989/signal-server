package org.whispersystems.textsecuregcm.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WXInfo {

    @JsonProperty
    private String openid;

    @JsonProperty
    private String nickname;

    @JsonProperty
    private int sex;

    @JsonProperty
    private String province;

    @JsonProperty
    private String city;

    @JsonProperty
    private String country;

    @JsonProperty
    private String headimgurl;

    @JsonProperty
    private String[] privilege;

    @JsonProperty
    private String unionid;
}
