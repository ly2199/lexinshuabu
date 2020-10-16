package com.example.step;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class PostActivity {
    public static String login(String username, String password) {
        HashMap<String, String> Mdata = new HashMap<String, String>();
        Mdata.put("appType","6");
        Mdata.put("clientId","88886");
        Mdata.put("loginName",username);            //存入数据
        Mdata.put("password",password);
        Mdata.put("roleType","0");
        JSONObject data = new JSONObject(Mdata);  //转化为json格式
        String result = sendJsonPost("https://sports.lifesense.com/sessions_service/login?systemType=2&version=4.6.7",data.toString(),null);
        return result;
    }
    public static String sendJsonPost(String apiUrl,String Json,String cookie) {

        String result = "";
        BufferedReader reader = null;
        try {
            String urlPath = apiUrl;
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            if (cookie!=null) {
                conn.setRequestProperty("Cookie", cookie);
            }
            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            conn.setRequestProperty("accept","*/*"); //此处为暴力方法设置接受所有类型，以此来防范返回415;
            conn.setRequestProperty("accept", "application/json");
            // 往服务器里面发送数据
            if (Json != null && !TextUtils.isEmpty(Json)) {
                byte[] writebytes = Json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes());
                outwritestream.flush();
                outwritestream.close();
                Log.d("hlhupload", "doJsonPost: conn" + conn.getResponseCode());
            }
            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();
            }else{result ="{\"code\":0,\"msg\":\"网络未连接或其他错误\"}";}
        } catch (Exception e) {
            result = e.getMessage();
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
