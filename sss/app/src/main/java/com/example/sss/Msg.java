package com.example.sss;

public class Msg {
    public static final int TYPE_RECEIVED = 0;//接受
    public static final int TYPE_SENT = 1;//发送
    private String content;//内容
    private int type;//消息类型
    public String getContent(){
        return content;
    }
    public int getType(){
        return type;
    }
    public Msg(String content,int type){
        this.content = content;
        this.type = type;
    }
}
