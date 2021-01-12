package com.example.sss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatRoom extends AppCompatActivity implements View.OnClickListener{
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private Button back;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private Socket socketSend;
    private String ip="192.168.43.154";
    private String port="5555";
    DataInputStream in;
    DataOutputStream out;
    boolean isRunning = false;
    private TextView myName;
    private String recMsg;
    private boolean isSend=false;
    private String name;
    private Context mcontext;
    private MyDBOpenHelper myDBHelper;


    private Handler handler = new Handler(Looper.myLooper()){//获取当前进程的Looper对象传给handler->消息传递
        @Override
        public void handleMessage(@NonNull Message msg){//
            if(!recMsg.isEmpty()){
                addNewMessage(recMsg,Msg.TYPE_RECEIVED);//添加新数据
            }
        }
    };
    public void save(Msg m) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String sql = "INSERT INTO lastmessage(message,type) values(?,?)";
        db.execSQL(sql,new String[]{m.getContent(), String.valueOf(m.getType())});
    }
    public void init() {
        Log.d("ttw","初始化开始");
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String sql = "SELECT * FROM lastmessage";
        Cursor cursor = db.rawQuery(sql,null);
        if(cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                String xiaoxi = cursor.getString(cursor.getColumnIndex("message"));
                Message message = new Message();
                message.obj = xiaoxi;
                handler.sendMessage(message);
            }
        }
        cursor.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        mcontext = ChatRoom.this;
        myDBHelper = new MyDBOpenHelper(mcontext,"my.db",null,1);
        Intent intent =getIntent();
        name=intent.getStringExtra("name");
        inputText = findViewById(R.id.input_text);
        send=findViewById(R.id.send);
        send.setOnClickListener(this);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder dialog= new AlertDialog.Builder(ChatRoom.this);
                dialog.setTitle("退出");
                dialog.setMessage("退出登录?");
                dialog.setCancelable(false);
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if(socketSend.isConnected())
                            socketSend.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }   finally {
//                            android.os.Process.killProcess(android.os.Process.myPid());
//                            System.exit(0);
                            finish();//finish()是在程序执行的过程中使用它来将对象销毁,finish（）方法用于结束一个Activity的生命周期
                        }

                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();//让返回键开始启动
            }
        });
        runOnUiThread(new Runnable() {//不断更新聊天框的信息
            @Override
            synchronized public void run() {
                LinearLayoutManager layoutManager = new LinearLayoutManager(ChatRoom.this);
                msgRecyclerView= findViewById(R.id.msg_recycler_view);
                msgRecyclerView.setLayoutManager(layoutManager);
                adapter = new MsgAdapter(msgList);
                msgRecyclerView.setAdapter(adapter);
            }
        });
        new Thread(new Runnable(){
            @Override
            synchronized public void run(){
                try{
                    if((socketSend = new Socket(ip,Integer.parseInt(port)))==null){
                        Log.d("read","发送消息1");
                    }
                    else{
                        isRunning = true;
                        Log.d("read","发送消息2");
                        in = new DataInputStream(socketSend.getInputStream());
                        out = new DataOutputStream(socketSend.getOutputStream());
                        new Thread(new receive(),"接收线程").start();
                        new Thread(new Send(),"发送线程").start();
                    }
                }catch(Exception e){
                    isRunning = false;
                    e.printStackTrace();
                    Looper.prepare();//创建消息循环
                    Toast.makeText(ChatRoom.this, "连接服务器失败QAQ", Toast.LENGTH_SHORT).show();//现实服务器链接失败
                    Looper.loop();
                    try{
                        socketSend.close();
                    }catch(IOException e1){
                        e1.printStackTrace();
                    }
                    finally {
                        finish();
                    }

                }
            }
        }).start();
       //init();
        Log.d("ttw","初始化完成");
    }
    public void addNewMessage(String msg,int type){
        Msg message = new Msg(msg,type);
        msgList.add(message);//加入消息链表
        adapter.notifyItemInserted(msgList.size()-1);//将我的adapter数据插入到position位置
        Toast.makeText(getApplicationContext(),"get新消息",Toast.LENGTH_SHORT).show();
        //msgRecyclerView.scrollToPosition(msgList.size()-1);//将屏幕滚到最下面，也就是新消息刷新后，滚动
    }
    class receive implements Runnable{
        synchronized public void run(){
            recMsg = "";
            while(isRunning){
                try{
                    recMsg = in.readUTF();
                    Log.d("ttw","收到了一条消息"+"recMsg: "+ recMsg);
                }catch(Exception e){
                    try {
                        in.close();
                        socketSend.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    finally {
                        System.exit(0);
                    }
                    e.printStackTrace();
                }
                if(!(TextUtils.isEmpty(recMsg)) && socketSend.isConnected()){//防止消息为null的时候
                    Log.d("ttw","inputStream:"+in);
                    save(new Msg(recMsg,Msg.TYPE_RECEIVED));
                    Message message = new Message();
                    message.obj=recMsg;
                    handler.sendMessage(message);
                }
            }
        }
    }
    @Override
    public void onClick(View view){
        String content = inputText.getText().toString();
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("hh:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(content).append("\n\n").append(date);
        content = sb.toString();
        if(!"".equals(content)){
            Msg msg = new Msg(content,Msg.TYPE_SENT);
            msgList.add(msg);
            adapter.notifyItemInserted(msgList.size()-1);
            msgRecyclerView.scrollToPosition(msgList.size()-1);
            isSend = true;
            inputText.setText("");//清空输入框
        }
        save(new Msg(content,Msg.TYPE_SENT));
        sb.delete(0,sb.length());
    }
    class Send implements Runnable{
        @Override
       synchronized public void run(){
            while(isRunning){
                String content = inputText.getText().toString();
                Log.d("ttw",content);
                Log.d("ttw","准备向服务器发消息");
                if(!"".equals(content)&&isSend){
                    @SuppressLint("SimpleDateFormat")
                    String date = new SimpleDateFormat("hh:mm:ss").format(new Date());
                    StringBuilder sb = new StringBuilder();
                    sb.append(content).append("\n\n来自：").append(name).append("\n").append(date);
                    content = sb.toString();
                    try{
                        out.writeUTF(content);//向服务器发送本条消息
                        sb.delete(0,sb.length());
                        Log.d("ttw","已经向服务器发送消息");
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    isSend = false;//状态更改
                    //inputText.setText("");//输入框清空
                }
            }
        }
    }

}
