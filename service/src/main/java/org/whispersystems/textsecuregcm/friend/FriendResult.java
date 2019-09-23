package org.whispersystems.textsecuregcm.friend;

import lombok.Getter;
import lombok.Setter;
import org.whispersystems.textsecuregcm.storage.Account;

/**
 * @author xiefan
 * @date 2019/9/18 10:04
 */

@Getter
@Setter
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
}
