package test;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {
    public static void main(String[] args) {
        Scanner scanner1 = new Scanner(System.in);
        System.out.print("请输入您的帐号:");
        //String userID=scanner1.next();
        String userID = "6995965022";       //juhkgf
        //String userID = "8809492112";     //juhkff
        String passWord = "aqko251068";
        final int LOGIN_PORT = 1111;
        final int SEND_PORT = 2222;
        try {
            String password = tools.Login.pswVerification(userID);
            if (password == null)
                System.out.println("null");                                           //（帐号不存在）登录失败
            else if (!password.equals(passWord))
                System.out.println("false");                                          //（密码错误）登录失败
            else if (password.equals(passWord))
                System.out.println("true");                                           //登录成功
            else
                System.out.println("error");                                          //其它情况
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String SERVER_IP = "123.207.13.112";
        //final String SERVER_IP = "localhost";
        SocketAddress socketAddress = new InetSocketAddress(SERVER_IP, LOGIN_PORT);
        //DatagramSocket ds = null;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();



            //ds.bind(socketAddress);
            System.out.println("ds绑定的本地端口:"+ds.getLocalPort());
            System.out.println("ds绑定的端口:"+ds.getPort());


        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] buff = userID.getBytes();
        byte[] buffer = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buff, 0, buff.length, socketAddress);
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //接收回馈
        dp.setData(buffer, 0, buffer.length);
        try {
            ds.receive(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = new String(dp.getData(), 0, dp.getLength());
        System.out.println("接收到的反馈:" + message);
        //message有两种可能：succeed即IP地址没有发生变化;update即IP地址与上次不同.


        ReceiveThread receiveMessage = new ReceiveThread(ds, SERVER_IP, SEND_PORT);
        receiveMessage.start();


        //下面是聊天区域
        SocketAddress socketAddress1 = new InetSocketAddress(SERVER_IP, SEND_PORT);                          //可不可以注释掉？
        try {
            ds.connect(socketAddress1);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        buff = userID.getBytes();
        dp = new DatagramPacket(buff, 0, buff.length, socketAddress1);
        try {


            //ds=new DatagramSocket();

            ds.send(dp);



            System.out.println("ds绑定的本地端口:"+ds.getLocalPort());
            System.out.println("ds绑定的端口:"+ds.getPort());


        } catch (IOException e) {
            e.printStackTrace();
        }

  /*      SendBlankThread sendBlankThread = new SendBlankThread(ds, userID);
        Thread thread = new Thread(sendBlankThread);
        thread.start();*/

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("输入您的帐号:");
            String senderID = scanner.next();
            System.out.print("输入您要发送给的用户的帐号:");
            String receiverID = scanner.next();
            System.out.print("输入您要说的话:");
            String sentence = scanner.next();
            String sendmessage = "Send:" + senderID + ":" + receiverID + ":" + sentence;
            //5+10+1+10+1=27个字符

            buff = sendmessage.getBytes();                                                                //发送的消息似乎有长度限制
            dp = new DatagramPacket(buff, 0, buff.length, socketAddress1);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ReceiveThread extends Thread {
    DatagramSocket ds;
    byte[] by;
    DatagramPacket dp;
    String message;
    SocketAddress socketAddress;

    public ReceiveThread(DatagramSocket ds, String SERVERIP, int PORT) {
        this.ds = ds;                                                             //客户端收发数据用同一个端口
        //this.dp = dp;
        this.socketAddress = new InetSocketAddress(SERVERIP, PORT);
        try {
            ds.connect(this.socketAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    //@Override
    public void start() {
        super.start();
    }

    //@Override
    public void run() {
        System.out.println("接受消息的线程启动!");
        while (true) {
            by = new byte[8 * 1024];
            dp = new DatagramPacket(by, 0, by.length, socketAddress);
            try {
                ds.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            message = new String(dp.getData(), 0, dp.getLength());
            if (!message.equals("success"))
                System.out.println("\n\n收到新消息!");


            System.out.print(message);

            /*String senderId = message.substring(0, 10);                        //截取获得发送方Id
            message = message.substring(11, message.length());                 //截取获得消息内容
            System.out.println("消息来自于帐号:" + senderId);
            System.out.println("消息内容:" + message);*/
        }
    }
}

class SendBlankThread implements Runnable {
    DatagramSocket ds;
    byte[] by;
    DatagramPacket dp;
    //String message;
    SocketAddress socketAddress;
    //String SERVERIP = "123.207.13.112";
    String SERVERIP="123.207.13.112";
    int PORT = 2222;
    String userID;

    public SendBlankThread(DatagramSocket ds, String userID) {
        this.ds = ds;
        try {
            ds.connect(socketAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }


        System.out.println("ds绑定的本地端口:"+ds.getLocalPort());
        System.out.println("ds绑定的端口:"+ds.getPort());


        this.socketAddress = new InetSocketAddress(SERVERIP, PORT);
        this.userID = userID;
        //by = userID.getBytes();
        //by="Connect".getBytes();
        by="".getBytes();
        this.dp = new DatagramPacket(by, 0, by.length, socketAddress);
    }

    @Override
    public void run() {
        try {
            while(true){
                ds.send(dp);
                Thread.sleep(100000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}