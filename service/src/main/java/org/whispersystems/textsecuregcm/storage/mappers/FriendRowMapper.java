package org.whispersystems.textsecuregcm.storage.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.whispersystems.textsecuregcm.friend.Friend;
import org.whispersystems.textsecuregcm.friend.FriendRequests;
import org.whispersystems.textsecuregcm.storage.Account;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author xiefan
 * @date 2019/9/25 10:22
 */
public class FriendRowMapper implements RowMapper<Friend> {

    @Override
    public Friend map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Friend(rs.getLong(FriendRequests.ID),rs.getString(FriendRequests.USER_NUMBER),rs.getString(FriendRequests.FRIEND_NUMBER),rs.getString("remark"),rs.getInt("status"));
    }
}
