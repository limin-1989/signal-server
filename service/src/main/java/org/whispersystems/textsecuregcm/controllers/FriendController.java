package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("v1/friend")
public class FriendController {

    private final Logger logger = LoggerFactory.getLogger(FriendController.class);

    private final AccountsManager accounts;

    public FriendController(AccountsManager accounts){
        this.accounts = accounts;
    }

    @Timed
    @PUT
    @Path("/query/{number}")
    public Response queryUser(@PathParam("number") String number){
        Optional<Account> account = accounts.get(number);


        return Response.ok().build();
    }

    @PUT
    @Path("/apply/{user_id}/{friend_id}/{reason}/{request_time}")
    public void applyForFriend(){

    }
}
