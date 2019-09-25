package org.whispersystems.textsecuregcm.friend;


import org.whispersystems.textsecuregcm.storage.Account;

/**
 * @author xiefan
 * @date 2019/9/18 10:04
 */


public class FriendResult {


    private Integer status;

    private Account account;

    private FriendResult(){}

    public static FriendResult createResult(Integer status,Account account){
        FriendResult friendResult = new FriendResult();
        friendResult.setStatus(status);
        friendResult.setAccount(account);

        return friendResult;

    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
