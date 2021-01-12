package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public static ArrayList<UserThread> socketList = new ArrayList<UserThread>();//创建一个泛型是UserThread（UserThread是下面的一个类）的动态数组，或者说用户线程池
    public static startServer startServer;

    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        new startServer().start();//new 一个线程对象开始启动（由于startServer类继承了Thread）
        String op;
        while(true) {
            System.out.println("@all 服务器广播");
            System.out.println("shutdown 关闭服务器");
            op = in.next();
            if(op.equals("@all")) {
                String context = in.nextLine();
                for(UserThread s : socketList) {
                    try {
                        s.getDos().writeUTF(context);
                    }
                    catch (IOException e) {
                        System.out.println("UserThread循环异常");
                        System.out.println(s.skt + " 用户已经下线");
                        try {
                            s.skt.close();
                        }
                        catch (IOException ee) {
                            ee.printStackTrace();
                        }
                        socketList.remove(s);//将s从动态数组socketList中删除
                        System.out.println("当前聊天人数:"+(socketList.size()));
                        e.printStackTrace();
                    }
                }
            }
            else if(op.equals("shutdown")) {
                System.exit(0);
            }
            else if(op.equals("kick")) {
                int kport = in.nextInt();
                for(UserThread s : socketList) {
                    if(s.skt.getPort()==kport) {
                        try {
                            s.skt.close();
                        }
                        catch (IOException ee) {
                            ee.printStackTrace();
                        }
                        finally {
                            socketList.remove(s);
                            break;
                        }
                    }
                }
            }
            else if(op.equals("cls")) {
                for(UserThread s : socketList) {
                        try {
                            s.getDos().writeUTF("清理不在线用户ing");
                        }
                        catch (IOException ee) {
                            try {
                                s.skt.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(s.skt+" 下线");
                            socketList.remove(s);
                            ee.printStackTrace();
                        }


                }
            }
            else {
                System.out.println("指令错误=_=");
            }
        }
    }

    static class startServer extends Thread{
        synchronized public void run(){
            try{
                ServerSocket serverSocket = new ServerSocket(5555);
                //创建端口值为：5555的ServerSocket对象
                System.out.println("Mchat服务器已开启等待连接^^");
                while(true){//死循环
                    Socket socket = serverSocket.accept();//创建socket对象，用于接受客户端的请求
                    System.out.println(""+socket+"进入Mchat服务器");//用于显示客户端的IP地址，客户端的端口号，以及电脑的端口号
                    UserThread userThread = new UserThread(socket);//通过下面定义的UserTread的有参构造，创建userThread对象
                    Server.socketList.add(userThread);
                    System.out.println("当前在线人数:"+socketList.size());
                    new Thread(userThread).start();//开启输入输出流线程
                }
            }catch(IOException e){
                System.out.println("startServer异常");
                e.printStackTrace();
            }
        }
    }
    static class UserThread implements Runnable{
        Socket skt;
        String usename;
        private DataOutputStream dos;
        private DataInputStream dis;
        public DataOutputStream getDos(){//返回输出流
            return dos;
        }
        public void setDos(DataOutputStream dos){//给输出流传递参数
            this.dos=dos;
        }
        public DataInputStream getDis(){//返回输入流
            return dis;
        }
        public void setDis(DataInputStream dis){//给输入流传递参数
            this.dis=dis;
        }
        public UserThread(Socket socket){//构造有参构造
            skt=socket;
        }
        @Override
        synchronized public void run(){
            try{
                dos= new DataOutputStream(skt.getOutputStream());//获取输出流（准备从服务器给其他的客户端发消息）
                dis= new DataInputStream(skt.getInputStream());//接收客户端发过来的消息（输入流）
                String recMsg ="";
                while(true){//使服务器无限循环
                    if(this.skt.isConnected() && socketList.size() != 0 && (!"".equals(recMsg = dis.readUTF()))){//读取输入流的消息，并把消息传到recMsg中
                        System.out.println("收到一条消息:"+ recMsg);//显示：收到一条消息+“传入的消息”
                        for(UserThread s : socketList){//增强for循环
                            if(s.equals(this)){
                                continue;
                            }
                            try{
                                s.getDos().writeUTF(recMsg);//将UTF-8的字符串写入字节流
                            }catch(IOException e){
                                System.out.println("UserThread循环异常");
                                System.out.println(s.skt + " 用户已经下线");
                                s.skt.close();
                                socketList.remove(s);//将s从动态数组socketList中删除
                                System.out.println("当前聊天人数:"+(socketList.size()));
                                e.printStackTrace();
                            }
                        }
                        recMsg="";//recMsg内容重新刷新
                    }
                }
            }catch(IOException e){
                try {
                    for(UserThread s : socketList) {
                        if(s != null) {
                            if (s.equals(this)) {
                                s.skt.close();
                                socketList.remove(s);
                                break;
                            }
                        }
                        else {
                            s.skt.close();;
                            socketList.remove(s);
                            System.out.println(s.skt + " 用户已经下线");
                            System.out.println("当前在线人数:"+socketList.size());
                        }
                    }
                    System.out.println(this.skt + " 用户已经下线");
                    System.out.println("当前在线人数:"+socketList.size());
                } catch (IOException ioException) {
                    System.out.println("下线异常");
                    ioException.printStackTrace();
                }
                System.out.println("UserThread大异常");
                e.printStackTrace();
            }
        }
    }
}
