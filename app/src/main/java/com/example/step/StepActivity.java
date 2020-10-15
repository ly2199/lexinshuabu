package com.example.step;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.util.Date;

public class StepActivity {
    private static String apiUrl = "https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2?version=4.5&systemType=2";
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String step(String userId, String accessToken, String step) {
        String cookie = "accessToken="+accessToken;
        int calories= (int) (Double.parseDouble(step)/4);
        int distance= (int) (Double.parseDouble(step)/3);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设定格式
        String measurementTime = df.format(new Date());// new Date()为获取当前系统时间
        String updated= String.valueOf(System.currentTimeMillis());
//        System.out.println(measurementTime);
//        System.out.println(updated);
        String data = "{\"list\":[{\"DataSource\":2,\"active\":1,\"calories\":"+calories+",\"dataSource\":2,\"deviceId\":\"M_NULL\",\"distance\":"+distance+",\"exerciseTime\":0,\"isUpload\":0,\"measurementTime\":\""+measurementTime+"\",\"priority\":0,\"step\":"+step+",\"type\":2,\"updated\":"+updated+",\"userId\":"+userId+"}]}";
        String result="";
        result = PostActivity.sendJsonPost(apiUrl,data,cookie);
        return result;
    }

}
