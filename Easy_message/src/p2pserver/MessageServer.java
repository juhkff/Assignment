package p2pserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import connection.Conn;
import model.message.ChatMessage;
import model.message.FileMessage;
import p2pserver.messageServerDao.Address;
import tools.Chat;
import tools.Online;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.*;
import java.sql.*;

public class MessageServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(1111);
        byte[] by;
        DatagramPacket dp;
        String message;


        while (true) {
            //System.out.println("wait...");
            by = new byte[1024 * 8];
            dp = new DatagramPacket(by, 0, by.length);
            //System.out.println("等待消息客户端上线...");
            ds.receive(dp);
            //创建基本线程对消息头进行判断以确定接下来的操作，同时不影响服务器接收下一个数据包
            BasicThread basicThread = new BasicThread(ds, dp);
            Thread thread = new Thread(basicThread);
            thread.start();
        }
    }

    //回传发送成功
    private static class CallBackThread implements Runnable{
        DatagramPacket dp;
        DatagramSocket ds;

        public CallBackThread(DatagramPacket dp, DatagramSocket ds) {
            this.dp = dp;
            this.ds = ds;
        }

        @Override
        public void run() {
            String message=new String(dp.getData(),0,dp.getLength());
            String senderID=message.split("/")[3];
            String receiverID=message.split("/")[2];
            String callBack=message.split("/")[0];                                          //不给客户端另一客户端的公网IP
            String receiverAddress=Chat.getReceiverAddress(receiverID);
            String ip=receiverAddress.split(":")[0];
            int port= Integer.parseInt(receiverAddress.split(":")[1]);
            SocketAddress socketAddress=new InetSocketAddress(ip,port);
            byte[] bytes=message.getBytes();
            dp=new DatagramPacket(bytes,0,bytes.length,socketAddress);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**服务器需要把聊天内容存入聊天表里，并将响应用户的isupdate设为1**/
    private final static class ChatThread implements Runnable{
        DatagramPacket dp;
        DatagramSocket ds;
        String message;
        String note;
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<ChatMessage>(){}.getType();
        public ChatThread(DatagramPacket dp, DatagramSocket ds) {
            this.dp = dp;
            this.ds = ds;
        }

        @Override
        public void run() {
            System.out.println("\n");
            message=new String(dp.getData(),0,dp.getLength());
            note=message.split("/")[2];
            System.out.println("note:\n"+note);
            ChatMessage chatMessage=this.gson.fromJson(note,this.type);
            try {
                System.out.println("发来的消息:"+chatMessage.getMessage());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
//            String senderID=chatMessage.getAnotherID();
            String senderID=chatMessage.getSenderID();
            String anotherID=chatMessage.getAnotherID();
            //byte nature=chatMessage.getNature();
            String content= null;
            try {
                content = chatMessage.getMessage();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String sendTime=chatMessage.getSendTime();
            Timestamp timestamp= Timestamp.valueOf(sendTime);
            try {
                Chat.insertChatMessageForServer(senderID,anotherID,content,timestamp);
//                Chat.updateContactStatus(senderID,anotherID);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String address=Chat.getReceiverAddress(anotherID);
            String ip=address.split(":")[0];
            int port= Integer.parseInt(address.split(":")[1]);
            SocketAddress socketAddress=new InetSocketAddress(ip,port);
            byte[] by=message.getBytes();
            dp=new DatagramPacket(by,0,by.length,socketAddress);
            try {
                ds.send(dp);
                System.out.println("转发完毕!发送到:"+ip+":"+port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //LoginThread既用作登陆时的地址写入，又用作心跳包的地址检测.
    private static class LoginThread implements Runnable {
        private DatagramPacket dp;
        private DatagramSocket ds;
        private String clientIP;
        private int clientPORT;
        private String address;
        private String localAddress;
        private byte[] by;
        private String message;
        private String userID;

        LoginThread(DatagramPacket dp, DatagramSocket ds) {
            this.dp = dp;
            this.ds = ds;
        }

        @Override
        public void run() {
            System.out.println();
            System.out.println();

            message = new String(dp.getData(), 0, dp.getLength());
            userID = message.split("/")[1];                                                        //从数据包内容中获得用户userID
            localAddress = message.split("/")[2];
            /* System.out.print("Ch客户端本次登录地址:");*/
            ;
            InetSocketAddress inetSocketAddress = (InetSocketAddress) dp.getSocketAddress();
            clientIP = inetSocketAddress.getAddress().getHostAddress();                                   //获取客户端IP
            clientPORT = inetSocketAddress.getPort();                                                     //获取发送/客户端端口
            address = clientIP + ":" + clientPORT;                                                        //获取客户端上线地址

            //if (!localAddress.equals("NO")) {
            System.out.println(/*"\n登录用户的本地局域网地址:" + localAddress + */"\n登录用户的公网映射地址:" + address + "\n");

            //将用户的(局域网和公网映射)地址存到数据库中
            Connection connection = Conn.getConnection();
            Statement statement = null;
            int[] result = {-1, -1};
            try {
                statement = connection.createStatement();
                String sql = "UPDATE userinfo SET localAddress=\'" + localAddress + "\' WHERE userID=" + userID;
                statement.addBatch(sql);
                sql = "UPDATE userinfo SET remoteAddress=\'" + address + "\' WHERE userID=" + userID;
                statement.addBatch(sql);
                result = statement.executeBatch();
            } catch (SQLException e) {
                System.out.println("用户" + userID + "登录失败!");
                try {
                    throw new Exception("用户" + userID + "登录失败!");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

            if (result[0] != 1 || result[1] != 1) {
                try {
                    throw new Exception("消息地址更新执行异常(本机局域网地址或公网映射地址异常)!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                by = "success".getBytes();
                dp.setData(by, 0, by.length);                                                     //若成功登录则给客户端发送success字符串
                try {
                    ds.send(dp);                                                                        //dp中还保留着发送地址
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final class HeartThread implements Runnable {
        private DatagramSocket ds;
        private DatagramPacket dp;
        private String message;
        private String current_Address;
        private String saved_Address;
        private byte[] by;


        public HeartThread(DatagramPacket dp, DatagramSocket ds) {
            this.dp = dp;
            this.ds = ds;
        }

        @Override
        public void run() {
            message = new String(dp.getData(), 0, dp.getLength()).split(":")[1];                             //获得userID
            Address address = new Address();
            try {
                this.saved_Address = address.getMessageAddress(message);                                                  //获得数据库中存储的客户端公网映射地址
                /**System.out.println("saved_Address:"+saved_Address);**/
                this.current_Address = ((InetSocketAddress) this.dp.getSocketAddress()).getAddress().getHostAddress()+":"+((InetSocketAddress) this.dp.getSocketAddress()).getPort();     //获得当前映射地址
                /**System.out.println("current_Address:"+current_Address);**/
                if (!saved_Address.equals(current_Address)) {
                    System.out.println("新旧地址不一样?");
                    dp.setData("PublicAddressChanged".getBytes(), 0, "PublicAddressChanged".getBytes().length);      //dp中还保留这来源地址
                    ds.send(dp);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class TransmitThread implements Runnable{
        private DatagramPacket dp;
        private DatagramSocket ds;
        private String message;
        private String receiverAddress;
        private SocketAddress socketAddress;
        private byte[] messageby;

        public TransmitThread(DatagramPacket dp, DatagramSocket ds) {
            this.dp = dp;
            this.ds = ds;
            this.message=new String(dp.getData(),0,dp.getLength());
            this.messageby=this.message.getBytes();
        }

        @Override
        public void run() {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<FileMessage>() {
            }.getType();
            FileMessage fileMessage=gson.fromJson(message.substring(9,message.length()),type);;
            System.out.println("在线发送请求:来自"+fileMessage.getSenderID()+"\t发送到"+fileMessage.getReceiverID()+"\t文件名"+fileMessage.getFileName()+"\t文件大小"+fileMessage.getFileSize());
            String receiverMessageAddr=Online.getMessageAddressByID(fileMessage.getReceiverID());
            this.receiverAddress=receiverMessageAddr;
            String ip=this.receiverAddress.split(":")[0];
            String port=this.receiverAddress.split(":")[1];
            this.socketAddress=new InetSocketAddress(ip, Integer.parseInt(port));
            this.dp=new DatagramPacket(messageby,0,messageby.length,socketAddress);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class BasicThread implements Runnable {
        private DatagramSocket ds;
        private DatagramPacket dp;
        private String message;
        private byte[] by;

        public BasicThread(DatagramSocket ds, DatagramPacket dp) {
            this.ds = ds;
            this.dp = dp;
        }

        //线程中创建线程会影响父线程的正常终止吗
        @Override
        public void run() {
            message = new String(dp.getData(), 0, dp.getLength());
            if (message.startsWith("Heartbeat")) {
                /**System.out.println("MessageServer Touch...");**/
                //启动登录线程对发送心跳包者执行检测地址操作
                HeartThread heartThread = new HeartThread(dp, ds);
                Thread thread = new Thread(heartThread);
                thread.start();
            } else if (message.startsWith("Login")) {
                System.out.print("客户端聊天系统上线!客户端本次登录地址:");
                /*try {
                    Thread.sleep(1000);                                             //保证客户端先出于receive状态?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                //启动登录线程对登陆者执行一系列上线操作
                LoginThread loginThread = new LoginThread(dp, ds);
                Thread thread = new Thread(loginThread);
                thread.start();
            }else if(message.startsWith("Chat")){
                /** 聊天 **/
                ChatThread chatThread=new ChatThread(dp,ds);
                Thread thread=new Thread(chatThread);
                thread.start();
            }else if(message.startsWith("CallBack")){
                CallBackThread callBackThread=new CallBackThread(dp,ds);
                Thread thread=new Thread(callBackThread);
                thread.start();
            }else if (message.startsWith("SendFile")) {
                TransmitThread transmitThread = new TransmitThread(dp, ds);
                Thread thread = new Thread(transmitThread);
                thread.start();
            }
        }
    }


}



