package org.whispersystems.textsecuregcm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.whispersystems.textsecuregcm.entities.AccountAttributes;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Path("/v1/wx")
public class WXLoginController {

    private String appid = "";
    private String secret = "";

    @GET
    @Path("/getAccessToken/{code}")
    public String getAccessToken(@PathParam("code") String code){
        String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code";
        String result = doGet(path);
        if (!result.equals("")) {
            JSONObject json = new JSONObject(result);
            String access_token = json.optString("access_token");
            String refresh_token = json.optString("refresh_token");
            String openid = json.optString("openid");
            String unionid = json.optString("unionid");

//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map map = objectMapper.readValue(result, Map.class);
//                String access_token = (String) map.get("access_token");
//                String refresh_token = (String) map.get("refresh_token");
//                String openid = (String) map.get("openid");
//                String unionid = (String) map.get("unionid");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            return access_token;
        } else {
            return code;
        }
    }

//    @GET
//    @Path("/login/{access_token}/{openid}")
//    public String login(@PathParam("access_token") String access_token, @PathParam("openid") String openid){
//        String path = "https://api.weixin.qq.com/sns/auth?access_token="+access_token+"&openid="+openid;
//        String result = doGet(path);
//        if(!result.equals("")){
//            JSONObject json = new JSONObject(result);
//            int errcode = json.optInt("errcode", -1);
//            if(errcode == 0){
//                return "ok";
//            }
//        }
//
//        return "";
//    }

    @GET
    @Path("/login/{code}")
    public Response login(@PathParam("code") String verificationCode,
                          @HeaderParam("Authorization")   String authorizationHeader,
                          @HeaderParam("X-Signal-Agent")  String userAgent,
                          @Valid AccountAttributes accountAttributes)
    {


        return Response.ok().entity("asdqwe").build();
    }

    public String doGet(String path){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("connection", "keep-Alive");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.connect();

            int code = conn.getResponseCode();
            if (code == 200){
                StringBuilder stringBuilder = new StringBuilder();
                String temp = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                while ((temp = br.readLine()) != null){
                    stringBuilder.append(temp);
                }
                br.close();
                return stringBuilder.toString();
            }
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return "";
    }

    public String doPost(String path, String params){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("connection", "keep-Alive");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            OutputStream out = conn.getOutputStream();
            out.write(params.getBytes());
            out.flush();
            out.close();

            int code = conn.getResponseCode();
            if(code == 200){
                StringBuilder stringBuilder = new StringBuilder();
                String temp = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                while ((temp = br.readLine()) != null){
                    stringBuilder.append(temp);
                }
                br.close();
                return stringBuilder.toString();
            }
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return "";
    }
}
