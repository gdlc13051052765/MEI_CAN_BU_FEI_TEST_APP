package com.example.rp3128_udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by user on 15-5-4.
 */

/* 发送udp多播 */
public  class udpBroadCast extends Thread {
    MulticastSocket sender = null;
    DatagramPacket dj = null;
    InetAddress group = null;
    //1，首先创建一个Handler对象
    Handler handler=new Handler();
    byte[] data = new byte[1024];
    String sendStr = "";


    //设置发送内容
    public udpBroadCast(String dataString) {
        data = dataString.getBytes();
    }

    @Override
    public void run() {
        try {
            sender = new MulticastSocket();
            group = InetAddress.getByName("127.0.0.1");
            dj = new DatagramPacket(data,data.length,group,6666);
            sender.send(dj);
            sender.close();
           // Log.i("udp发送ip地址:", "127.0.0.1");
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
           // handler.postDelayed(this, 100);//100ms
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}