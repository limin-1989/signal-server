package org.whispersystems.textsecuregcm.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Priority;

/**
 * @author xiefan
 * @date 2019/9/10 10:06
 */
public class Friend {

    @JsonProperty
    private String id;

    @JsonProperty
    private String user_number;

    @JsonProperty
    private String friend_number;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_number() {
        return user_number;
    }

    public void setUser_number(String user_number) {
        this.user_number = user_number;
    }

    public String getFriend_number() {
        return friend_number;
    }

    public void setFriend_number(String friend_number) {
        this.friend_number = friend_number;
    }
}
