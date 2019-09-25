package org.whispersystems.textsecuregcm.storage.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.whispersystems.textsecuregcm.friend.Friend;
import org.whispersystems.textsecuregcm.friend.FriendRequest;
import org.whispersystems.textsecuregcm.friend.FriendRequests;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author xiefan
 * @date 2019/9/25 10:36
 */
public class FriendRequestRowMapper implements RowMapper<FriendRequest> {
    @Override
    public FriendRequest map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new FriendRequest(rs.getLong(FriendRequests.ID)
                                 ,rs.getString(FriendRequests.USER_NUMBER),
                                   rs.getString(FriendRequests.FRIEND_NUMBER),
                                    rs.getString(FriendRequests.REASON),
                                      rs.getString("request_time"),rs.getInt("status"));
    }
}
