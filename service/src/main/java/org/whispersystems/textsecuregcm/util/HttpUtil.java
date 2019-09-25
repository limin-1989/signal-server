package org.whispersystems.textsecuregcm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {

    public static String jPush = "https://api.jpush.cn/v3/push";

    public static String doGet(String path){
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

    public static String doPost(String path, String params){
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
