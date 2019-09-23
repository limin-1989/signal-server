package org.whispersystems.textsecuregcm.controllers;

import com.amazonaws.HttpMethod;
import com.codahale.metrics.annotation.Timed;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.whispersystems.textsecuregcm.entities.AttachmentDescriptorV2;
import org.whispersystems.textsecuregcm.entities.AttachmentUri;
import org.whispersystems.textsecuregcm.limits.RateLimiter;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.s3.PolicySigner;
import org.whispersystems.textsecuregcm.s3.PostPolicyGenerator;
import org.whispersystems.textsecuregcm.s3.UrlSigner;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.util.Pair;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import io.dropwizard.auth.Auth;
import org.xmlpull.v1.XmlPullParserException;

@Path("/v2/attachments")
public class AttachmentControllerV2 extends AttachmentControllerBase {

  private final PostPolicyGenerator policyGenerator;
  private final PolicySigner        policySigner;
  private final RateLimiter         rateLimiter;

  public AttachmentControllerV2(RateLimiters rateLimiters, String accessKey, String accessSecret, String region, String bucket) {
    this.rateLimiter      = rateLimiters.getAttachmentLimiter();
    this.policyGenerator  = new PostPolicyGenerator(region, bucket, accessKey);
    this.policySigner     = new PolicySigner(accessSecret, region);
  }

  @Timed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/form/upload")
  public AttachmentDescriptorV2 getAttachmentUploadForm(@Auth Account account) throws RateLimitExceededException, MinioException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, IOException {
    rateLimiter.validate(account.getNumber());

    ZonedDateTime        now          = ZonedDateTime.now(ZoneOffset.UTC);
    long                 attachmentId = generateAttachmentId();
    String               objectName   = String.valueOf(attachmentId);
    Pair<String, String> policy       = policyGenerator.createFor(now, String.valueOf(objectName));
    String               signature    = policySigner.getSignature(now, policy.second());


    UrlSigner urlSigner = new UrlSigner("PN6D8RRWCQXWZ9ZWRN7G", "m9STTJoYNj3HXmg6NdLt3mi1Imnf89XpQeyD0SrP", "cxcv");

    String geturl = urlSigner.geturl("attachments", String.valueOf(attachmentId), HttpMethod.PUT);




    System.out.println(geturl);



    return new AttachmentDescriptorV2(attachmentId, objectName, policy.first(),
                                      "private", "AWS4-HMAC-SHA256",
                                      now.format(PostPolicyGenerator.AWS_DATE_TIME),
                                      policy.second(), signature);
  }


}
