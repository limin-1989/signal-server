package org.whispersystems.textsecuregcm.friend;


import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import okhttp3.HttpUrl;
import org.whispersystems.textsecuregcm.entities.AttachmentUri;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * @author xiefan
 * @date 2019/9/11 17:34
 */
public class Test {

    public static void main(String[] args) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidExpiresRangeException {


        long l = System.currentTimeMillis();
        Date date = new Date();

        System.out.println(date);





    }

}
