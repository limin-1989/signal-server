package org.whispersystems.textsecuregcm.friend;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import org.whispersystems.textsecuregcm.storage.FaultTolerantDatabase;
import org.whispersystems.textsecuregcm.storage.Messages;
import org.whispersystems.textsecuregcm.storage.mappers.AccountRowMapper;
import org.whispersystems.textsecuregcm.util.Constants;

import java.util.List;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author xiefan
 * @date 2019/9/4 18:22
 */
public class FriendRequests {

    private static final String ID = "id";
    private static final String USER_NUMBER = "user_number";
    private static final String FRIEND_NUMBER = "friend_number";
    private static final String REASON = "reason";
    private static final String REQUEST_TIME = "request_time";

    private final MetricRegistry metricRegistry        = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
    private final Timer findTimer                = metricRegistry.timer(name(Messages.class, "find"          ));
    private final Timer storeTimer               = metricRegistry.timer(name(Messages.class, "store"         ));
    private final Timer QueryAllRequestTimer     = metricRegistry.timer(name(Messages.class, "QueryAllRequest"   ));
    private final Timer FindFriendTimer          = metricRegistry.timer(name(Messages.class, "FindFriend"    ));
    private final Timer deleteRequestTimer          = metricRegistry.timer(name(Messages.class, "FindFriend"    ));



    private final FaultTolerantDatabase database;

    public FriendRequests(FaultTolerantDatabase database) {
        this.database = database;
        this.database.getDatabase().registerRowMapper(new AccountRowMapper());
    }

    /**
     * 查找好友请求表中是否已有相同的请求
     * @param selfNumber
     * @param number
     * @return
     */
    public Optional<FriendRequest> find(String selfNumber, String number){
        return database.with(jdbi -> jdbi.withHandle(handle -> {
            try(Timer.Context ignored = findTimer.time()) {
                return handle.createQuery("SELECT * FROM friend_request where"+USER_NUMBER+"= :user_number AND"+FRIEND_NUMBER+"= :friend_number")
                        .bind("user_number",selfNumber)
                        .bind("friend_number",number)
                        .mapTo(FriendRequest.class)
                        .findFirst();
            }
        }));
    }

    /**
     * 增加好友请求
     * @param selfNumber
     * @param number
     */
    public void store(String selfNumber, String number,String reason){
        database.use(jdbi -> jdbi.useHandle(handle -> {
            try (Timer.Context ignored = storeTimer.time()) {
                handle.createUpdate("INSERT INTO friend_request("+USER_NUMBER+","+FRIEND_NUMBER+","+","+REASON+","+REQUEST_TIME+") " +
                        "VALUES (:user_number,:friend_number,:reason,:request_time)")
                        .bind("user_number", selfNumber)
                        .bind("friend_number",number )
                        .bind("reason",reason)
                        .bind("request_time",System.currentTimeMillis())
                        .execute();
            }
        }));
    }


    public void deleteRequest(String selfNumber, String number){
        database.use(jdbi ->jdbi.useHandle(handle -> {
            try (Timer.Context ignored = deleteRequestTimer.time()) {
                handle.createUpdate("DELETE FROM friend_request WHERE"+USER_NUMBER+"=:user_number AND"+FRIEND_NUMBER+"=:friend_number")
                        .bind("user_number",selfNumber)
                        .bind("friend_number",number)
                        .execute();
            }
        }));
    }



    /**
     * 查找好友请求
     * @param number
     * @return
     */
    public List<FriendRequest> findRequestByUserNumber(String number){
        return database.with(jdbi -> jdbi.withHandle(handle -> {
            try (Timer.Context ignored = QueryAllRequestTimer.time()){
                return handle.createQuery("SELECT * FROM friend_request WHERE"+FRIEND_NUMBER + "= :friend_number")
                        .bind("friend_number",number)
                        .mapTo(FriendRequest.class)
                        .list();
            }
        }));
    }

    /**
     * 查找好友关系
     * @param number
     * @return
     */
    public Optional<Friend> friendFriendRelationshipByNumber(String selfNumber, String number){
        return database.with(jdbi -> jdbi.withHandle(handle -> {
            try(Timer.Context ignored =FindFriendTimer.time() ) {
                return handle.createQuery("SELECT * FROM friends WHERE "+USER_NUMBER+" = :user_number AND "+FRIEND_NUMBER+" = :friend_number")
                        .bind("user_number",selfNumber)
                        .bind("friend_number",number)
                        .mapTo(Friend.class)
                        .findFirst();
            }
        }));
    }


    /**
     * 增加好友关系
     * @param selfNumber
     * @param number
     */
   public void queryFriend(String selfNumber,String number){
       database.use(jdbi -> jdbi.useHandle(handle -> {
           try (Timer.Context ignored = storeTimer.time()) {
               handle.createUpdate("INSERT INTO friends("+USER_NUMBER+","+FRIEND_NUMBER+")" +
                       "VALUES (:user_number,:friend_number)")
                       .bind("user_number", selfNumber)
                       .bind("friend_number",number )
                       .execute();
           }
       }));
   }


    /**
     * 查找好友请求
     * @param number
     * @return
     */
    public List<String> findFriendByUserNumber(String number){
        return database.with(jdbi -> jdbi.withHandle(handle -> {
            try (Timer.Context ignored = QueryAllRequestTimer.time()){
                return handle.createQuery("SELECT friend_number FROM friends WHERE"+USER_NUMBER + "= :user_number")
                        .bind("user_number",number)
                        .mapTo(String.class)
                        .list();
            }
        }));
    }




}
