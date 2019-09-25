package org.whispersystems.textsecuregcm.friend;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.Anonymous;
import org.whispersystems.textsecuregcm.auth.OptionalAccess;
import org.whispersystems.textsecuregcm.controllers.*;
import org.whispersystems.textsecuregcm.entities.*;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.Accounts;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.Util;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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



    private  Accounts accounts;
    private FriendRequests friendRequests ;
    private  RateLimiters rateLimiters;
    private  AccountsManager accountsManager;
    private MessageController messageController;

    public FriendController(Accounts accounts,FriendRequests friendRequests,RateLimiters rateLimiters,AccountsManager accountsManager,MessageController messageController){

        this.accounts = accounts;
        this.friendRequests = friendRequests;
        this.rateLimiters = rateLimiters;
        this.accountsManager = accountsManager;
        this.messageController = messageController;

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
    public FriendResult searchUser(@Auth Account account, @PathParam("number")String number){

        if (Util.isEmpty(number)) {
            logger.info("Invalid number: " + number);
            throw new WebApplicationException(Response.status(400).build());
        }


            Optional<Account> friend = accounts.get(number);
            if (friend.isEmpty()){
                return FriendResult.createResult(400,null);
            }else {
                return FriendResult.createResult(200,friend.get());
            }

    }

    /**
     * 发送好友请求
     * @param account
     * @param number
     * @return
     */
    @Timed
    @Path("/addFriend/{number}/{reason}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void addFriendRequest(@Auth Account account, @PathParam("number")String number,@PathParam("reason")String reason){

        if (Util.isEmpty(number)) {
            logger.info("Invalid number: " + number);
            throw new WebApplicationException(Response.status(400).build());
        }
         String selfNumber = account.getNumber();
         Integer status = searchFriendByNumber(selfNumber, number);
        if (status == SearchFriendEnum.SUCCESS.status){
            sendFriendrequest(selfNumber,number,reason);

        }
    }





//    @Timed
//    @Path("/{destination}")
//    @PUT
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public SendMessageResponse sendMessage(@Auth                                     Optional<Account>   source,
//                                           @HeaderParam(OptionalAccess.UNIDENTIFIED) Optional<Anonymous> accessKey,
//                                           @PathParam("destination")                 String              destinationName,
//                                           @Valid IncomingMessageList messages)
//            throws RateLimitExceededException
//    {
//        if (!source.isPresent() && !accessKey.isPresent()) {
//            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
//        }
//
//        if (source.isPresent() && !source.get().getNumber().equals(destinationName)) {
//            rateLimiters.getMessagesLimiter().validate(source.get().getNumber() + "__" + destinationName);
//        }
//
//        if (source.isPresent() && !source.get().getNumber().equals(destinationName)) {
//            identifiedMeter.mark();
//        } else {
//            unidentifiedMeter.mark();
//        }
//
//        try {
//            boolean isSyncMessage = source.isPresent() && source.get().getNumber().equals(destinationName);
//
//            Optional<Account> destination;
//
//            if (!isSyncMessage) destination = accountsManager.get(destinationName);
//            else                destination = source;
//
//            OptionalAccess.verify(source, accessKey, destination);
//            assert(destination.isPresent());
//
//            messageController.validateCompleteDeviceList(destination.get(), messages.getMessages(), isSyncMessage);
//            messageController.validateRegistrationIds(destination.get(), messages.getMessages());
//
//            System.out.println("messages size is "+messages.getMessages().size()+" !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            for (IncomingMessage incomingMessage : messages.getMessages()) {
//                Optional<Device> destinationDevice = destination.get().getDevice(incomingMessage.getDestinationDeviceId());
//
//                if (destinationDevice.isPresent()) {
//                    System.out.println("message timestamp is "+messages.getTimestamp()+", online is "+messages.isOnline());
//                    messageController.sendMessage(source, destination.get(), destinationDevice.get(), messages.getTimestamp(), messages.isOnline(), incomingMessage);
//
//
//                    sendFriendrequest(source.get().getNumber(),destinationName,incomingMessage.getContent());
//                }
//            }
//
//            return new SendMessageResponse(!isSyncMessage && source.isPresent() && source.get().getActiveDeviceCount() > 1);
//        } catch (NoSuchUserException e) {
//            throw new WebApplicationException(Response.status(404).build());
//        } catch (MismatchedDevicesException e) {
//            throw new WebApplicationException(Response.status(409)
//                    .type(MediaType.APPLICATION_JSON_TYPE)
//                    .entity(new MismatchedDevices(e.getMissingDevices(),
//                            e.getExtraDevices()))
//                    .build());
//        } catch (StaleDevicesException e) {
//            throw new WebApplicationException(Response.status(410)
//                    .type(MediaType.APPLICATION_JSON)
//                    .entity(new StaleDevices(e.getStaleDevices()))
//                    .build());
//        }
//    }


    @Timed
    @DELETE
    @Path("/refuse/{destination}")
    public void refuse(@Auth Account account,@PathParam("destination")  String  destinationName){
        if (Util.isEmpty(account.getNumber())|| Util.isEmpty(destinationName)) {

            throw new NullPointerException();
        }
        friendRequests.deleteRequest( destinationName,account.getNumber());
    }

    @Timed
    @PUT
    @Path("/agree/{destination}")
    public void agree(@Auth Account account,@PathParam("destination")  String  destinationName){
        if (Util.isEmpty(account.getNumber())|| Util.isEmpty(destinationName)) {
             throw new NullPointerException();
        }
        friendRequests.queryFriend(account.getNumber(),destinationName);
        friendRequests.queryFriend(destinationName,account.getNumber());
        friendRequests.deleteRequest( destinationName,account.getNumber());

    }


    @Timed
    @GET
    @Path("/allFriend")
    public List<Friend> allFriend(@Auth Account account){

        String number = account.getNumber();
        List<Friend> friends = new ArrayList<>();
        if (!number.isEmpty()){
            friends=friendRequests.findFriendByUserNumber(number);
        }

        return friends;
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
        Optional<Friend> relationship = friendRequests.friendFriendRelationshipByNumber(selfNumber, number);
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
    public void  sendFriendrequest(String selfNumber,String number,String reason){

        //在好友请求表中查找记录
        Optional<FriendRequest> friendRequest = friendRequests.find(selfNumber, number);
        if (!friendRequest.isPresent()){
            friendRequests.store(selfNumber, number,reason);

        }else {
            friendRequests.deleteRequest(selfNumber, number);
            friendRequests.store(selfNumber, number, reason);
        }
    }


    public List<FriendRequest> queryFriendRequest(String number){
        final List<FriendRequest> friendRequests = this.friendRequests.findRequestByUserNumber(number);
        if (friendRequests ==null ||friendRequests.size()<1){
            return null;
        }
        return friendRequests;
    }
}
