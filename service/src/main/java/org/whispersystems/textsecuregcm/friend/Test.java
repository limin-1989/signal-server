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
import java.util.List;

/**
 * @author xiefan
 * @date 2019/9/11 17:34
 */
public class Test {

    public static void main(String[] args) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidExpiresRangeException {


        String cxcv1 = new MinioClient("http://192.168.2.116:9000", "PN6D8RRWCQXWZ9ZWRN7G", "m9STTJoYNj3HXmg6NdLt3mi1Imnf89XpQeyD0SrP")
                .presignedGetObject("cxcv", "578218164793434104");
        System.out.println(cxcv1);





    }

}
