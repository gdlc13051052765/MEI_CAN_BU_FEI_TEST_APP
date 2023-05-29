package com.example.rp3128_udp;

public class UserInfo {
    public String name; // 姓名
    public String uuid; // 用户卡号
    public int weight; // 当前重量

    public UserInfo(String name, String uuid,  int weight) {
        this.name = name;
        this.uuid = uuid;
        this.weight = weight;
    }
}
