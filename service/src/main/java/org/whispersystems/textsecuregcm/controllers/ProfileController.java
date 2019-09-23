package org.whispersystems.textsecuregcm.controllers;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.codahale.metrics.annotation.Timed;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.whispersystems.textsecuregcm.auth.OptionalAccess;
import org.whispersystems.textsecuregcm.auth.Anonymous;
import org.whispersystems.textsecuregcm.auth.UnidentifiedAccessChecksum;
import org.whispersystems.textsecuregcm.configuration.ProfilesConfiguration;
import org.whispersystems.textsecuregcm.entities.Profile;
import org.whispersystems.textsecuregcm.entities.ProfileAvatarUploadAttributes;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.s3.PolicySigner;
import org.whispersystems.textsecuregcm.s3.PostPolicyGenerator;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.util.Pair;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import io.dropwizard.auth.Auth;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Path("/v1/profile")
public class ProfileController {

  private final RateLimiters     rateLimiters;
  private final AccountsManager  accountsManager;

  private final PolicySigner        policySigner;
  private final PostPolicyGenerator policyGenerator;

  private final AmazonS3            s3client;
  private final String              bucket;
  private final MinioClient minioClient;

  public ProfileController(RateLimiters rateLimiters,
                           AccountsManager accountsManager,
                           ProfilesConfiguration profilesConfiguration) throws InvalidPortException, InvalidEndpointException {
    AWSCredentials         credentials         = new BasicAWSCredentials(profilesConfiguration.getAccessKey(), profilesConfiguration.getAccessSecret());
    AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);

    this.rateLimiters       = rateLimiters;
    this.accountsManager    = accountsManager;
    this.bucket             = profilesConfiguration.getBucket();
//    this.s3client           = AmazonS3Client.builder()
//                                            .withCredentials(credentialsProvider)
//                                            .withRegion(profilesConfiguration.getRegion())
//                                            .build();

    ClientConfiguration clientConfiguration = new ClientConfiguration();
    clientConfiguration.setSignerOverride("AWSS3V4SignerType");

    this.s3client           = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://192.168.2.160:9000", "us-east-1"))
            .withPathStyleAccessEnabled(true)
            .withClientConfiguration(clientConfiguration)
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();

    this.policyGenerator  = new PostPolicyGenerator(profilesConfiguration.getRegion(),
                                                    profilesConfiguration.getBucket(),
                                                    profilesConfiguration.getAccessKey());

    this.policySigner     = new PolicySigner(profilesConfiguration.getAccessSecret(),
                                             profilesConfiguration.getRegion());

    this.minioClient = new MinioClient(profilesConfiguration.getEndpoint(), profilesConfiguration.getAccessKey(), profilesConfiguration.getAccessSecret());
  }

  @Timed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{number}")
  public Profile getProfile(@Auth                                     Optional<Account>   requestAccount,
                            @HeaderParam(OptionalAccess.UNIDENTIFIED) Optional<Anonymous> accessKey,
                            @PathParam("number")                      String number,
                            @QueryParam("ca")                         boolean useCaCertificate)
      throws RateLimitExceededException
  {
    if (!requestAccount.isPresent() && !accessKey.isPresent()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    if (requestAccount.isPresent()) {
      rateLimiters.getProfileLimiter().validate(requestAccount.get().getNumber());
    }

    Optional<Account> accountProfile = accountsManager.get(number);
    OptionalAccess.verify(requestAccount, accessKey, accountProfile);

    //noinspection ConstantConditions,OptionalGetWithoutIsPresent
    return new Profile(accountProfile.get().getProfileName(),
                       accountProfile.get().getAvatar(),
                       accountProfile.get().getIdentityKey(),
                       accountProfile.get().isUnauthenticatedDeliverySupported() ? UnidentifiedAccessChecksum.generateFor(accountProfile.get().getUnidentifiedAccessKey()) : null,
                       accountProfile.get().isUnrestrictedUnidentifiedAccess());
  }

  @Timed
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/name/{name}")
  public void setProfile(@Auth Account account, @PathParam("name") @UnwrapValidatedValue(true) @Length(min = 72,max= 72) Optional<String> name) {
    account.setProfileName(name.orElse(null));
    accountsManager.update(account);
  }


  @Timed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/form/avatar")
  public ProfileAvatarUploadAttributes getAvatarUploadForm(@Auth Account account) {
    String               previousAvatar = account.getAvatar();
    ZonedDateTime        now            = ZonedDateTime.now(ZoneOffset.UTC);
    String               objectName     = generateAvatarObjectName();
    Pair<String, String> policy         = policyGenerator.createFor(now, objectName);
    String               signature      = policySigner.getSignature(now, policy.second());

    if (previousAvatar != null && previousAvatar.startsWith("profiles/")) {
//      s3client.deleteObject(bucket, previousAvatar);
      try {
        minioClient.removeObject(bucket, previousAvatar);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    account.setAvatar(objectName);
    accountsManager.update(account);

    String url = "";
    try {
      url = minioClient.presignedPutObject("profiles", objectName, 60 * 60 * 24);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new ProfileAvatarUploadAttributes(objectName, policy.first(), "private", "AWS4-HMAC-SHA256",
                                             now.format(PostPolicyGenerator.AWS_DATE_TIME), policy.second(), signature, url);
  }

  private String generateAvatarObjectName() {
    byte[] object = new byte[16];
    new SecureRandom().nextBytes(object);

    return Base64.encodeBase64URLSafeString(object);
  }
}
