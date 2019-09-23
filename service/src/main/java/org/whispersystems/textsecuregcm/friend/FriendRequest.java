package org.whispersystems.textsecuregcm.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author xiefan
 * @date 2019/9/5 9:53
 */
public class FriendRequest {

    @JsonProperty
    private String id;

    @JsonProperty
    private String user_number;

    @JsonProperty
    private String friend_number;

    @JsonProperty
    private String reason;

    @JsonProperty
    private String request_time;

    public FriendRequest(String id, String user_number, String friend_number, String reason, String request_time) {
        this.id = id;
        this.user_number = user_number;
        this.friend_number = friend_number;
        this.reason = reason;
        this.request_time = request_time;
    }


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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequest_time() {
        return request_time;
    }

    public void setRequest_time(String request_time) {
        this.request_time = request_time;
    }
}
