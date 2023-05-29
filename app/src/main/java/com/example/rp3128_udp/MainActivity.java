package com.example.rp3128_udp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import java.io.IOException;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import java.io.BufferedReader;


import android.widget.TextView;
//import com.example.rp3128_udp.bean.UserInfo;
import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity {
    Button startBroadCast;
    Button stopBroadCast;

    TextView send_label;
    TextView receive_label;
    TextView disp_weigh_ditextView;

    //定时udp发送任务句柄
    Thread udpSendThread;

    public static final String TAG = "MainActivity";

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    public udpSendDataJson mudpSendDataJson;
    public String udpSendData = "";

    private boolean mRunning = false;
    Process process = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* start 按钮 和 stop 按钮 的 初始化 */
        startBroadCast = (Button) findViewById(R.id.start);
        stopBroadCast = (Button) findViewById(R.id.stop);

        send_label = (TextView) findViewById(R.id.send_information);
        receive_label = (TextView) findViewById(R.id.receive_information);
        disp_weigh_ditextView = (TextView) findViewById(R.id.disp_weigh_ditextView);

        send_label.append("\n\n");
        receive_label.append("\n\n");

        startBroadCast.setOnClickListener(listener);
        stopBroadCast.setOnClickListener(listener);

        mHandlerThread = new HandlerThread("THREAD_NAME");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(mUdpSendRunnable);//将线程post到Handler中



        //udp 接收信息回调
        Handler handler_for_udpReceiveAndtcpSend = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                /* 0x111: 在TextView "send_label" 上, append 发送tcp连接的信息 */
                if (msg.what == 0x111) {
                    send_label.append((msg.obj).toString());
                }
                /* 0x222: 在TextView上 "receive_label" 加上收到tcp连接的信息, udp多播的信息 */
                else if (msg.what == 0x222 ) {
                    receive_label.setText("");
                    //获取当前系统时间
                    long timecurrentTimeMillis = System.currentTimeMillis();
                    SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS\n",Locale.getDefault());
                    String time1 = sdf.format(timecurrentTimeMillis);
                    receive_label.append(time1);
                    receive_label.append((msg.obj).toString());

                    //JSONObject jsonObject = new JSONObject((msg.obj).toString());
                    // 把JSON串转换为UserInfo类型的对象
                    Log.i("udp json 解析数据", (msg.obj).toString());
                    if(checkisJson((msg.obj).toString())==true){
                        UserInfo newUser = new Gson().fromJson((msg.obj).toString(), UserInfo.class);
                        String desc = String.format("\n\t姓名: %s\n\tUUID: %s\n\t重量: %d", newUser.name, newUser.uuid, newUser.weight);

                        //设置字体颜色
                        disp_weigh_ditextView.setTextColor(0xff00b5eb);
                        //设置字体大小
                        disp_weigh_ditextView.setTextSize(50);
                        //设置显示内容
                        disp_weigh_ditextView.setText(desc);
                    } else {
                        Log.i("udp json 解析数据", "json 格式错误");
                    }
                }
            }
        };

        /*接收udp多播 并 发送tcp 连接*/
        new udpReceiveAndtcpSend(handler_for_udpReceiveAndtcpSend).start();
        //udp 发送线程任务
        //udpSendThread = new udpBroadCast("udp send test");

        //创建udp发送数据json类
        mudpSendDataJson = new udpSendDataJson("getWeigh","");
        Gson sendgson = new Gson();
        udpSendData = sendgson.toJson(mudpSendDataJson);
        Log.i("", udpSendData);
    }

    //实现耗时操作的线程 定时通过回环地址发送udp数据
    Runnable mUdpSendRunnable = new Runnable() {
        @Override
        public void run() {
            //----------模拟耗时的操作，开始---------------
            try {
                if(mRunning == true){
                    //Log.i(TAG, "udp send thread running!");

                    //设置发送内容
                    new udpBroadCast(udpSendData).start();
                }
                //100ms 任务延时
                mHandler.postDelayed(this, 100);//100ms
            } catch (Exception e) {
                e.printStackTrace();
            }
            //----------模拟耗时的操作，结束---------------
        }
    };

    public void start_sh() {
//        try {
//            @SuppressWarnings("unused")
//            //Process proc = Runtime.getRuntime().exec("su -c ./data/meican/start.sh");
//            Process process = new ProcessBuilder("su -c ./data/meican/start.sh").redirectErrorStream(true).start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            String[] cmd = new String[]{"sh /data/meican/start.sh"};
            Process ps = Runtime.getRuntime().exec("sh /data/meican/start.sh");
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println("方法1：");
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRunning = false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁线程
        mHandler.removeCallbacks(mUdpSendRunnable);
    }

    //检查json合法性
    public static boolean checkisJson(String Json) {
        try {
            new JSONObject(Json);
        } catch (JSONException ex) {
            try {
                new JSONArray(Json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == startBroadCast ) {
                startBroadCast.setEnabled(false);
                stopBroadCast.setEnabled(true);
                //udpSendThread.setSendStr("meican udp send start");
                //udpSendThread.start()
                mUdpSendRunnable.run();
                mRunning = true;
//                try{
//                    String cmd = "su -c ./data/meican/start.sh";
//                    process = Runtime.getRuntime().exec(cmd);
//
//                }
//                catch (IOException e){
//                }
                start_sh();
            }
            else {
                startBroadCast.setEnabled(true);
                stopBroadCast.setEnabled(false);
                mRunning = false;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
