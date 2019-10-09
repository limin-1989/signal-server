package org.whispersystems.textsecuregcm.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Priority;

/**
 * @author xiefan
 * @date 2019/9/10 10:06
 */
public class Friend {

    @JsonProperty
    private long id;

    @JsonProperty
    private String user_number;

    @JsonProperty
    private String friend_number;

    @JsonProperty
    private String remark;

    @JsonProperty
    private int status;

    public Friend(){}

    public Friend(long id, String user_number, String friend_number, String remark, int status) {
        this.id = id;
        this.user_number = user_number;
        this.friend_number = friend_number;
        this.remark = remark;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
