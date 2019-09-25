package org.whispersystems.textsecuregcm.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Priority;

/**
 * @author xiefan
 * @date 2019/9/10 10:06
 */
public class Friend {

    @JsonProperty
    private int id;

    @JsonProperty
    private String user_number;

    @JsonProperty
    private String friend_number;

    public Friend() {
    }

    public Friend(int id, String user_number, String friend_number) {
        this.id = id;
        this.user_number = user_number;
        this.friend_number = friend_number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserNumber() {
        return user_number;
    }

    public void setUserNumber(String user_number) {
        this.user_number = user_number;
    }

    public String getFriendNumber() {
        return friend_number;
    }

    public void setFriendNumber(String friend_number) {
        this.friend_number = friend_number;
    }
}
