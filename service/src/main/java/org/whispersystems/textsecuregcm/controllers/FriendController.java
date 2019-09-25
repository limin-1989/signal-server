//package org.whispersystems.textsecuregcm.controllers;
//
//import com.codahale.metrics.annotation.Timed;
//import io.dropwizard.auth.Auth;
//import io.minio.MinioClient;
//import io.minio.errors.InvalidEndpointException;
//import io.minio.errors.InvalidPortException;
//import io.minio.errors.MinioException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.whispersystems.textsecuregcm.storage.Account;
//import org.whispersystems.textsecuregcm.storage.AccountsManager;
//import org.xmlpull.v1.XmlPullParserException;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.core.Response;
//import java.io.IOException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.util.Optional;
//
//@Path("v1/friend")
//public class FriendController {
//
//    private final Logger logger = LoggerFactory.getLogger(FriendController.class);
//
//    private final AccountsManager accounts;
//
//    public FriendController(AccountsManager accounts){
//        this.accounts = accounts;
//    }
//
//    @Timed
//    @GET
//    @Path("/query/{number}")
//    public Response queryUser(@PathParam("number") String number){
//        Optional<Account> des = accounts.get(number);
//
//        if (!des.isPresent()){
//            return Response.status(404).build();
//        }
//
//        String name = des.get().getName();
//        String avatar = des.get().getAvatar();
//        String result = "{\"number\":" + number + ",\"name\":" + name + ",\"avatar\":" + avatar + "}";
//
//        return Response.ok().entity(result).build();
//    }
//
//    @PUT
//    @Path("/apply/{user_id}/{friend_id}/{reason}/{request_time}")
//    public void applyForFriend(){
//
//    }
//
//    @GET
//    @Path("/list")
//    public Response getFriendsList(@Auth Account account){
//        String number = account.getNumber();
//
//
//        return Response.ok().build();
//    }
//
//    @GET
//    @Path("/test")
//    public void test() throws InvalidPortException, InvalidEndpointException {
//        MinioClient minioClient = new MinioClient("http://192.168.2.160:9000", "3UR9EMG74SKJ7WNBK0O2", "OL6+YfD2pUWncnjD8AMhnNRf9prIWI+pAjf0KKGC");
//        try {
////            minioClient.putObject("signal",  "test", "C:\\Users\\Administrator\\Desktop\\1.txt");
////            System.out.println("island.jpg is uploaded successfully");
////
////            String url = minioClient.presignedGetObject("signal", "test", 60 * 60 * 24);
////            System.out.println(url);
//
//            String url = minioClient.presignedPutObject("attachments", "1111111", 60 * 60 * 24);
//            System.out.println(url);
//
//        } catch(MinioException | NoSuchAlgorithmException | IOException | InvalidKeyException | XmlPullParserException e) {
//            System.out.println("Error occurred: " + e);
//        }
//    }
//}
