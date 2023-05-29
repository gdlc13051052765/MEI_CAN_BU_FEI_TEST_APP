package com.example.rp3128_udp;

//udp 发送json格式类
public class udpSendDataJson {
    private String cmd;
    private String data;

    public udpSendDataJson() {
        super();
    }
    public udpSendDataJson(String cmd, String data) {
        super();
        this.cmd = cmd;
        this.data = data;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
