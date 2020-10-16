package com.example.step;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.util.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button exit_bt;
    private Button exit_b;
    private Button login_bt;
    private Button shuxin;
    private Button logout;
    private TextView find_psw;
    private TextView step_now;
    private String username;
    private String password;
    private String sub_step_d="10";
    private EditText et_username;
    private EditText et_password;
    private EditText sub_step;
    private long exitTime;
    private String userId = "";
    private String accessToken= "";
    @RequiresApi(api = Build.VERSION_CODES.N)

    /*
    hashmap的排序方法
     */
    private static class ValueComparator implements Comparator<Map.Entry<Integer, Integer>>
    {
        public int compare(Map.Entry<Integer,Integer> m, Map.Entry<Integer,Integer> n)
        {
            return n.getValue()-m.getValue();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)   //解决版本问题
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*
        解决主线程不能访问网络的问题
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {

        /*
        初始化ui
         */
        exit_bt = findViewById(R.id.exit_bt);
        login_bt = findViewById(R.id.login_bt);
        find_psw = findViewById(R.id.find_psw);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        try{
         /*
         检查cookie是否过期
          */
        userId=load().getString("userId");
        accessToken=load().getString("accessToken");
        String step_data = StepActivity.step(userId, accessToken,sub_step_d);
        JSONObject step_status=new JSONObject(step_data);
        System.out.println(step_data);
        if (step_status.getInt("code")==200){
            step_update(step_data);
        }else{
                show_message(step_status.getString("msg"),3000);
            }
        }catch (JSONException e) {
            show_message("意外错误"+e.getMessage(),3000);
            e.printStackTrace();
        }

        /*
        退出按钮监听
         */
        exit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    show_message("再按一次退出程序",1100);
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }

            }
        });

        /*
        密码找回按钮监听
         */
        find_psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_message("暂未提供，请通过官方渠道恢复！",1200);

            }
        });
        
        login_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取用户名和密码 getText().toString().trim();
                username=et_username.getText().toString().trim();
                password=et_password.getText().toString().trim();
                //对当前密码进行md5
                String md5Psw= MD5Utils.md5(password);

                /*
                输入合法性检查
                 */
                if(TextUtils.isEmpty(username)){
                    show_message("请输入用户名",1200);
                    return;
                }else if(TextUtils.isEmpty(password)){
                    show_message("请输入密码",1200);
                    return;
                }

                /*
                开始登录
                 */
                String login_static = PostActivity.login(username,md5Psw);
                try {
                    JSONObject re_login=new JSONObject(login_static);
                    int code = re_login.getInt("code");
                    String msg = re_login.getString("msg");
                    if (code==200){
                        show_message("登录成功！",1200);
                        String save_data = re_login.getString("data");
                        save(save_data);   //获取到cookie并保存

                        /*
                        获取步数信息并跳转页面
                         */
                        userId=load().getString("userId");
                        accessToken=load().getString("accessToken");
                        String step_data = StepActivity.step(userId, accessToken,sub_step_d);
                        JSONObject step_status=new JSONObject(step_data);
                        if (step_status.getInt("code")==200){
                            step_update(step_data);
                        }else {
                            show_message("跳转失败："+step_status.getString("msg"),1200);

                        }
                    }else{
                        final Toast toast=Toast.makeText(getApplicationContext(), "登录失败："+msg, Toast.LENGTH_LONG);
                        show_message("登录失败："+msg,1200);

                    }

                } catch (JSONException e) {
                    show_message(login_static,1200);
                    e.printStackTrace();
                }
            }

        });

    }

    /*
    步数更新页面
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void step_update(final String step_data) throws JSONException {
        Integer get_step = 0;  //保存获取到的当前步数
        HashMap<Integer,Integer> all_step = new HashMap<Integer,Integer>();
        List<Map.Entry<Integer,Integer>> list=new ArrayList<>();

        /*
        设置页面并初始化ui
         */
        setContentView(R.layout.step_main);
        step_now = findViewById(R.id.step_now);
        sub_step = findViewById(R.id.sub_step);
        shuxin = findViewById(R.id.shuaxin);
        exit_b = findViewById(R.id.exit_b);
        logout = findViewById(R.id.logout);

        /*
        设置输入只接受数值
         */
        DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false,true);
        sub_step.setKeyListener(numericOnlyListener);

        /*
        读取获取到的信息并格式化
         */
        JSONObject step_status=new JSONObject(step_data);
        JSONObject jsonObject=step_status.getJSONObject("data");
        JSONArray jsonArray=jsonObject.getJSONArray("pedometerRecordHourlyList");
        JSONObject step_status_data=jsonArray.getJSONObject(0);
        String [] step= step_status_data.getString("step").split(",");

        /*
        24小时步数排序
         */
        for (int i=0;i<step.length;i++){
            all_step.put(i,Integer.valueOf(step[i]));
        }
        list.addAll(all_step.entrySet());
        ValueComparator vc=new ValueComparator();
        Collections.sort(list,vc);
        get_step=list.get(0).getValue();

        /*
        设置当前步数并显示
         */
        CharSequence charSequence = "当前步数："+get_step;
        step_now.setText(charSequence);
        final Integer finalGet_step = get_step;

        shuxin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                try {
                    boolean s=true;  //定义条件判断
                    sub_step_d = sub_step.getText().toString().trim();

                    /*
                    输入合法性检查
                     */
                    if(TextUtils.isEmpty(sub_step_d)||sub_step_d.length()>6){
                        s=false;
                        sub_step_d="0";
                        show_message("请输入步数并不大于40000",1200);
                    }else if (Integer.valueOf(sub_step_d)< finalGet_step){
                        s=false;
                        show_message("输入步数小于当前步数！",1200);
                    }else if (Integer.valueOf(sub_step_d)>40000){
                        s=false;
                        show_message("输入步数请小于40000！",1200);
                    }
                    if (s) {
                        String step_data = StepActivity.step(userId, accessToken, sub_step_d);  //更新数据提交
                        JSONObject step_status = null;

                        /*
                        更新状态判断
                         */
                        step_status = new JSONObject(step_data);
                        if (step_status.getInt("code") == 200) {
                            show_message("更新成功！",1500);
                            step_update(step_data);
                        }
                    }

                } catch (JSONException e) {
                    show_message("更新失败"+e.getMessage(),3000);
                    e.printStackTrace();
                }
            }
        });

        exit_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    show_message("再按一次退出程序",1100);
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }

            }
        });

        /*
        注销登录
         */
        logout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
            save("{\"userId\":\"27511458\",\"accessToken\":\"D2A6AFB93531605DBE56DC\"}");
            setContentView(R.layout.activity_main);
            init();
            }
        });

    }

    /*
    配置信息读取
     */
    private JSONObject load() throws JSONException {
        FileInputStream in=null;
        BufferedReader reader=null;
        StringBuilder content=new StringBuilder();
        try {

            in=openFileInput("login_access");
            reader=new BufferedReader(new InputStreamReader(in));
            String line="";
            //一行一行读取
            while((line=reader.readLine())!=null){
                content.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject load_data_g =new JSONObject(content.toString());

        return load_data_g;

    }

    /*
    保存cookie
     */
    private void save(String login_data) {
        FileOutputStream out=null;
        BufferedWriter writer=null;
        try {
            out=openFileOutput("login_access", Context.MODE_PRIVATE);
            writer=new BufferedWriter(new OutputStreamWriter(out));
            writer.write(login_data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /*
    调用系统弹框显示提示信息
     */
    private void show_message(String msg,int time){
        final Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                toast.cancel();
            }
        }, time);
    }

    @Override
    public void onClick(View v) {}
}
