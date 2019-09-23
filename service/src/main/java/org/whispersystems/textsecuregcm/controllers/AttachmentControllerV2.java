package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.whispersystems.textsecuregcm.entities.AttachmentDescriptorV2;
import org.whispersystems.textsecuregcm.limits.RateLimiter;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.s3.PolicySigner;
import org.whispersystems.textsecuregcm.s3.PostPolicyGenerator;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.util.Pair;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.dropwizard.auth.Auth;
import org.xmlpull.v1.XmlPullParserException;

@Path("/v2/attachments")
public class AttachmentControllerV2 extends AttachmentControllerBase {

  private final PostPolicyGenerator policyGenerator;
  private final PolicySigner        policySigner;
  private final RateLimiter         rateLimiter;
  private final MinioClient minioClient;

  public AttachmentControllerV2(RateLimiters rateLimiters, String accessKey, String accessSecret, String region, String bucket, String endpoint) throws InvalidPortException, InvalidEndpointException {
    this.rateLimiter      = rateLimiters.getAttachmentLimiter();
    this.policyGenerator  = new PostPolicyGenerator(region, bucket, accessKey);
    this.policySigner     = new PolicySigner(accessSecret, region);
    this.minioClient = new MinioClient(endpoint, accessKey, accessSecret);
  }

  @Timed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/form/upload")
  public AttachmentDescriptorV2 getAttachmentUploadForm(@Auth Account account) throws RateLimitExceededException, InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidExpiresRangeException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException {
    rateLimiter.validate(account.getNumber());

    ZonedDateTime        now          = ZonedDateTime.now(ZoneOffset.UTC);
    long                 attachmentId = generateAttachmentId();
    String               objectName   = String.valueOf(attachmentId);
    Pair<String, String> policy       = policyGenerator.createFor(now, String.valueOf(objectName));
    String               signature    = policySigner.getSignature(now, policy.second());

    String url = minioClient.presignedPutObject("attachments", objectName, 60 * 60 * 24);

    return new AttachmentDescriptorV2(attachmentId, objectName, policy.first(),
                                      "private", "AWS4-HMAC-SHA256",
                                      now.format(PostPolicyGenerator.AWS_DATE_TIME),
                                      policy.second(), signature, url);
  }


}
