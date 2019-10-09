package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.annotation.Timed;
import com.google.protobuf.ByteString;
import io.dropwizard.auth.Auth;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.entities.*;
import org.whispersystems.textsecuregcm.friend.*;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.push.NotPushRegisteredException;
import org.whispersystems.textsecuregcm.push.PushSender;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.Accounts;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.Util;
import org.whispersystems.textsecuregcm.websocket.WebsocketAddress;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author xiefan
 * @date 2019/9/4 15:11
 */

@Path("/v1/friend")
public class FriendController {

    private final Logger logger                 = LoggerFactory.getLogger(AccountController.class);
    private final MetricRegistry metricRegistry    = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
    private final Meter unidentifiedMeter = metricRegistry.meter(name(getClass(), "delivery", "unidentified"));
    private final Meter          identifiedMeter   = metricRegistry.meter(name(getClass(), "delivery", "identified"  ));

    private String path = "https://api.jpush.cn/v3/push";
    private String appKey = "d858203336cef4067364b4d3";
    private String masterSecret = "d94292fd80b985008a047124";

    private  Accounts accounts;
    private FriendRequests friendRequests ;
    private  RateLimiters rateLimiters;
    private  AccountsManager accountsManager;
    private MessageController messageController;
    private final PushSender pushSender;

    public FriendController(Accounts accounts,FriendRequests friendRequests,RateLimiters rateLimiters,AccountsManager accountsManager,MessageController messageController,PushSender pushSender){

        this.accounts = accounts;
        this.friendRequests = friendRequests;
        this.rateLimiters = rateLimiters;
        this.accountsManager = accountsManager;
        this.messageController = messageController;
        this.pushSender = pushSender;
    }

    /**
     * 查找用户，通过电话号码匹配
     * @param account
     * @param number
     * @return
     */
    @GET
    @Path("/search/{number}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchUser(@Auth Account account, @PathParam("number")String number){

        if (Util.isEmpty(number)) {
            logger.info("Invalid number: " + number);
            throw new WebApplicationException(Response.status(400).build());
        }

        Optional<Account> friend = accounts.get(number);
        if (friend.isEmpty()){
            return Response.status(400).build();
        }else {
            JSONObject json = new JSONObject();
            json.put("number", number);
            json.put("name", friend.get().getName());
            json.put("avatar", friend.get().getAvatar());

            return Response.ok().entity(json.toString()).build();
        }
    }

    /**
     * 发送好友请求
     * @param account
     * @param number
     * @return
     */
    @Timed
    @Path("/apply/{number}/{reason}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFriendRequest(@Auth Account account, @PathParam("number")String number,@PathParam("reason")String reason) {

        if (Util.isEmpty(number)) {
            logger.info("Invalid number: " + number);
            throw new WebApplicationException(Response.status(400).build());
        }

        long time = System.currentTimeMillis();
        String selfNumber = account.getNumber();
        Integer status = searchFriendByNumber(selfNumber, number);
        if (status == SearchFriendEnum.SUCCESS.status){
            sendFriendrequest(selfNumber,number,reason,time);
            // 发送好友请求推送
//            pushFriendRequest(account, number, reason);

            try {
                Account destinationAccount = accounts.get(number).get();
                Device destinationDevice = destinationAccount.getDevice(1).get();

                String srcNumber = account.getNumber();
                if (Util.isEmpty(srcNumber)) {
                    return Response.status(404).entity("error").build();
                }

                String srcName = account.getName();
                if (Util.isEmpty(srcName)) {
                    srcName = srcNumber;
                }

                String srcAvatar = account.getAvatar();
                if (Util.isEmpty(srcAvatar)) {
                    srcAvatar = "";
                }

                if (Util.isEmpty(reason)) {
                    reason = "";
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("number", srcNumber);
                jsonObject.put("name", srcName);
                jsonObject.put("avatar", srcAvatar);
                jsonObject.put("reason", reason);
                jsonObject.put("status", 0);
                jsonObject.put("time", time);

                WebsocketAddress address = new WebsocketAddress(destinationAccount.getNumber(), destinationAccount.getDevice(1).get().getId());

                MessageProtos.Envelope.Builder messageBuilder = MessageProtos.Envelope.newBuilder();

                messageBuilder.setType(MessageProtos.Envelope.Type.FRIEND_REQUEST)
                              .setTimestamp(System.currentTimeMillis())
                              .setServerTimestamp(System.currentTimeMillis());

                messageBuilder.setSource(account.getNumber())
                        .setSourceDevice((int)account.getAuthenticatedDevice().get().getId());

//                messageBuilder.setLegacyMessage(ByteString.copyFrom("test".getBytes()));

                messageBuilder.setContent(ByteString.copyFrom(jsonObject.toString().getBytes()));

                pushSender.sendMessage(destinationAccount, destinationDevice, messageBuilder.build(), true);

                return Response.ok().entity("success").build();
            } catch (NotPushRegisteredException e) {
                e.printStackTrace();
                return Response.status(404).entity("error").build();
            }
        }
        return Response.status(status).entity(SearchFriendEnum.getMsgByKey(status)).build();
    }

    @Timed
    @DELETE
    @Path("/refuse/{destination}")
    public Response refuse(@Auth Account account,@PathParam("destination")  String  destinationName){
        if (Util.isEmpty(account.getNumber())) {
            return Response.status(1).entity("account number is empty").build();
        }

        if (Util.isEmpty(destinationName)) {
            return Response.status(2).entity("destination number is empty").build();
        }

        friendRequests.deleteRequest(destinationName,account.getNumber());

        return Response.ok().entity("success").build();
    }

    @Timed
    @PUT
    @Path("/agree/{destination}")
    public Response agree(@Auth Account account, @PathParam("destination") String destinationName){
        if (Util.isEmpty(account.getNumber())) {
            return Response.status(1).entity("account number is empty").build();
        }

        if (Util.isEmpty(destinationName)) {
            return Response.status(2).entity("destination number is empty").build();
        }

        Optional<FriendRequest> friendRequest = friendRequests.find(destinationName, account.getNumber());

        Optional<Friend> friendDes = friendRequests.findFriendRelationshipByNumber(account.getNumber(), destinationName);
        if (friendDes.isPresent()) {
            if (friendRequest.isPresent()) {
                friendRequests.deleteRequest(destinationName, account.getNumber());
            }

            return Response.status(3).entity("we are already friends, do not add again").build();
        }

        friendRequests.addFriend(account.getNumber(), destinationName);

        Optional<Friend> friendSrc = friendRequests.findFriendRelationshipByNumber(destinationName, account.getNumber());
        if (!friendSrc.isPresent()) {
            friendRequests.addFriend(destinationName, account.getNumber());
        }

        if (friendRequest.isPresent()) {
            friendRequests.deleteRequest(destinationName, account.getNumber());
        }

        return Response.ok().entity("success").build();
    }

    /**
     * 获取好友列表
     * @param account
     * @return
     */
    @Timed
    @GET
    @Path("/allFriend")
    public Response allFriend(@Auth Account account){
        JSONArray result = new JSONArray();
        String number = account.getNumber();
        List<String> friendNumbers;
        Optional<Account> friendAccount;
        if (!number.isEmpty()){
            friendNumbers = friendRequests.findFriendByUserNumber(number);
            if (friendNumbers != null && friendNumbers.size() > 0) {
                for (int i = 0; i < friendNumbers.size(); i++) {
                    friendAccount = accounts.get(friendNumbers.get(i));
                    if (friendAccount.isPresent()) {
                        JSONObject json = new JSONObject();
                        json.put("number", friendAccount.get().getNumber());
                        json.put("name", friendAccount.get().getProfileName());
                        json.put("avatar", friendAccount.get().getAvatar());
                        result.put(json);
                    }
                }
                return Response.ok().entity(result.toString()).build();
            }
            return Response.status(2).entity("no friend").build();
        }
        return Response.status(1).entity("account number is empty").build();
    }

    @Timed
    @DELETE
    @Path("/delete/{number}")
    public Response deleteFriend(@Auth Account account, @PathParam("number") String number){
        if (Util.isEmpty(account.getNumber())) {
            return Response.status(1).entity("account number is empty").build();
        }

        if (Util.isEmpty(number)) {
            return Response.status(2).entity("destination number is empty").build();
        }

        friendRequests.deleteFriend(account.getNumber(), number);

        return Response.ok().build();
    }

    /**
     * 查找用户的所有的好友请求
     * @param account
     * @return
     */
    @Timed
    @GET
    @Path("/queryFriendRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FriendRequest> queryFriendRequest(@Auth Account account){
         String accountNumber = account.getNumber();
        if (!Util.isValidNumber(accountNumber)) {
            logger.info("Invalid number: " + accountNumber);
            throw new WebApplicationException(Response.status(400).build());
        }

         List<FriendRequest> friendRequests =queryFriendRequest(accountNumber);

        return friendRequests;
    }

    /**
     * 在用户表中查找判断好友
     * @param selfNumber
     * @param number
     * @return
     */
    public Integer searchFriendByNumber(String selfNumber,String number){

         Optional<Account> account = accounts.get(number);

        //无此用户
        if (account.isEmpty()){
            return SearchFriendEnum.USER_NOT_EXIST.status;
        }

        //此用户是自己，
        if (account.get().getNumber().equals(selfNumber)){
            return SearchFriendEnum.NOT_YOURSELF.status;
        }

        //此用户已经是自己的好友
        Optional<Friend> relationship = friendRequests.findFriendRelationshipByNumber(selfNumber, number);
        if (relationship.isPresent()){
            return SearchFriendEnum.ALREADY_FRIENDS.status;
        }

        //正常返回
        return  SearchFriendEnum.SUCCESS.status;
    }

    /**
     * 增加申请好友记录
     * @param selfNumber 发送申请好友方号码
     * @param number 接受好友申请方号码
     */
    public void  sendFriendrequest(String selfNumber,String number,String reason,long time){

        //在好友请求表中查找记录
        Optional<FriendRequest> friendRequest = friendRequests.find(selfNumber, number);
        if (!friendRequest.isPresent()){
            friendRequests.store(selfNumber, number,reason,time);

        }else {
            friendRequests.deleteRequest(selfNumber, number);
            friendRequests.store(selfNumber, number, reason,time);
        }
    }

    public List<FriendRequest> queryFriendRequest(String number){
        final List<FriendRequest> friendRequests = this.friendRequests.findRequestByUserNumber(number);
        if (friendRequests ==null ||friendRequests.size()<1){
            return null;
        }
        return friendRequests;
    }

    public void pushFriendRequest(Account account, String number, String reason){
        Optional<Account> des = accounts.get(number);

        JSONObject push = new JSONObject();
        JSONObject audience = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject android = new JSONObject();
        JSONObject intent = new JSONObject();
        JSONObject extras = new JSONObject();

        android.put("alert", "新的好友请求");
        intent.put("url", "点击通知跳转的页面");
        android.put("intent", intent);
        extras.put("sourceName", account.getName());
        extras.put("sourceHead", account.getAvatar());
        extras.put("reason", reason);
        android.put("extras", extras);
        notification.put("android", android);

        push.put("platform", "android");
        JSONArray registration_id = new JSONArray();
        registration_id.put(des.get().getDevice(1).get().getGcmId());
        audience.put("registration_id", registration_id);
        push.put("audience", audience);
        push.put("notification", notification);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((appKey+":"+masterSecret).getBytes("UTF-8")));
            conn.setRequestProperty("Content-Type", "application/Json; charset=UTF-8");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            OutputStream out = conn.getOutputStream();
            out.write(push.toString().getBytes());
            out.flush();
            out.close();

//            int code = conn.getResponseCode();
//            if(code == 200){
//                StringBuilder stringBuilder = new StringBuilder();
//                String temp = "";
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//                while ((temp = br.readLine()) != null){
//                    stringBuilder.append(temp);
//                }
//                br.close();
//            }
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                conn.disconnect();
            }
        }
    }
}
