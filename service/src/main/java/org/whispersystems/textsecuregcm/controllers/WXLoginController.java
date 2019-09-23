package org.whispersystems.textsecuregcm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.whispersystems.textsecuregcm.entities.AccountAttributes;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;
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

    @GET
    @Path("/upload")
    public void upload(){
        File file = new File("C:\\Users\\Administrator\\Desktop\\111111111.jpeg");
        uploadFile(file, "http://192.168.2.160:9000/attachments/1111111?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=3UR9EMG74SKJ7WNBK0O2%2F20190912%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20190912T070515Z&X-Amz-Expires=86400&X-Amz-SignedHeaders=host&X-Amz-Signature=671537ec97fa26669e5be28ecbe3df729e27e609f281a2e517581d27df493ca6");
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

    public void uploadFile(File file, String url){
        String boundary = "wk_file_2519775";
        String nextLine = "\r\n";
        String twoHyphens = "--";
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        FileInputStream inputStream = null;
        try {
            //获取HTTPURLConnection连接
            connection = createConnection(url, "PUT");
            //运行写入默认为false，置为true
            connection.setDoOutput(true);
            //禁用缓存
            connection.setUseCaches(false);
            //设置接收编码
            connection.setRequestProperty("Accept-Charset", "utf-8");
            //开启长连接可以持续传输
            connection.setRequestProperty("Connection", "keep-alive");
            //设置请求参数格式以及boundary分割线
            connection.setRequestProperty("Content-Type", "multipart/form-data");
            //设置接收返回值的格式
            connection.setRequestProperty("Accept", "application/json");
            //开启连接
            connection.connect();

            //获取http写入流
            outputStream = new DataOutputStream(connection.getOutputStream());

            //分隔符头部
            String header = twoHyphens + boundary + nextLine;
            //分隔符参数设置
            header += "Content-Disposition: form-data;name=\"file\";" + "filename=\"" + file.getName() + "\"" + nextLine + nextLine;
            //写入输出流
//            outputStream.write(header.getBytes());

            //读取文件并写入
            inputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = inputStream.read(bytes))!= -1){
                outputStream.write(bytes, 0, length);
            }
            //文件写入完成后加回车
//            outputStream.write(nextLine.getBytes());

            //写入结束分隔符
            String footer = nextLine + twoHyphens + boundary + twoHyphens + nextLine;
//            outputStream.write(footer.getBytes());
            outputStream.flush();
            //文件上传完成
            InputStream response = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(response);
            while (reader.read() != -1){
                System.out.println(new String(bytes, "UTF-8"));
            }
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                System.out.println(connection.getResponseMessage());
            }else {
                System.err.println("上传失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null){
                    outputStream.close();
                }
                if (inputStream != null){
                    inputStream.close();
                }
                if (connection != null){
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public HttpURLConnection createConnection(String urlPath, String method) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod(method);

        httpURLConnection.setRequestProperty("Charsert", "UTF-8");
        return httpURLConnection;
    }
}
