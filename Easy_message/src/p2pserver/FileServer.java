package p2pserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import connection.Conn;
import model.file.FileInfo;
import model.file.MyFileInfo;
import p2pserver.fileServerDao.Address;
import tools.file.model.UDPUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class FileServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(2222);
        byte[] by = new byte[1024 * 2];
        DatagramPacket dp = new DatagramPacket(by, 0, by.length);


        while (true) {
            //System.out.println("wait...");
            by = new byte[1024 * 2];
            dp = new DatagramPacket(by, 0, by.length);
            //System.out.println("等待文件客户端上线...");
            ds.receive(dp);
            //System.out.println("接收到!");
            BasicThread basicThread = new BasicThread(ds, dp);
            Thread thread = new Thread(basicThread);
            thread.start();
        }
    }

    public static  Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
    public static Type type=new TypeToken<FileInfo>(){}.getType();

    public static Gson mygson=new GsonBuilder().enableComplexMapKeySerialization().create();
    public static Type mytype=new TypeToken<MyFileInfo>(){}.getType();

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

        public LoginThread(DatagramPacket dp, DatagramSocket ds) {
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
            /*System.out.print("客户端文件系统上线!客户端本次登录地址:");*/
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
                String sql = "UPDATE userinfo SET localFileAddress=\'" + localAddress + "\' WHERE userID=" + userID;
                statement.addBatch(sql);
                sql = "UPDATE userinfo SET remoteFileAddress=\'" + address + "\' WHERE userID=" + userID;
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
            /*} else if (localAddress.equals("NO")) {
                Connection connection = Conn.getConnection();
                String sql = "UPDATE userinfo SET remoteFileAddress=? WHERE userID=?";
                int result = -1;
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, address);
                    preparedStatement.setString(2, userID);
                    result = preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("用户" + userID + "登录失败!");
                    try {
                        throw new Exception("用户" + userID + "登录失败!");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                if (result != 1) {
                    try {
                        throw new Exception("文件地址更新执行异常(公网映射地址异常)!");
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
            }*/
        }
    }

    private static final class HeartThread implements Runnable {
        private DatagramSocket ds;
        private DatagramPacket dp;
        private String message;
        private String current_Address;
        private String saved_Address;
        private byte[] by;
        Address address = new Address();

        public HeartThread(DatagramPacket dp, DatagramSocket ds) {
            this.dp = dp;
            this.ds = ds;
        }

        @Override
        public void run() {
            message = new String(dp.getData(), 0, dp.getLength()).split(":")[1];                             //获得userID
            try {
                this.saved_Address = address.getFileAddress(message);                                                  //获得数据库中存储的客户端公网映射地址
                this.current_Address = ((InetSocketAddress) this.dp.getSocketAddress()).getAddress().getHostAddress() + ":" + ((InetSocketAddress) this.dp.getSocketAddress()).getPort();     //获得当前映射地址
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

    /*private static class TransmitThread implements Runnable{
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
            this.receiverAddress=fileMessage.getReceiverAddress();
            String ip=this.receiverAddress.split(",")[0];
            String port=this.receiverAddress.split(",")[1];
            this.socketAddress=new InetSocketAddress(ip, Integer.parseInt(port));
            this.dp=new DatagramPacket(messageby,0,messageby.length,socketAddress);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    private static class TransThread implements Runnable{
        private String message;
        private DatagramSocket ds;
        private DatagramPacket dp;
        private String fileName;
        private String senderID;
        private String receiverID;
        public TransThread(String message, DatagramSocket ds,DatagramPacket dp) {
            this.message = message;
            this.ds = ds;
            this.dp=dp;
        }

        @Override
        public void run() {
            //System.out.println("收到!");
            message=message.substring(10,message.length());         //去掉报头
            //System.out.println("去掉头!");
            //System.out.println(message);
            FileInfo fileInfo=gson.fromJson(message,type);          //获得对象
            this.fileName=fileInfo.getFileName();
            this.senderID=fileInfo.getSenderID();
            //System.out.println("fileName: "+fileName+"\tsenderID: "+senderID);
            this.receiverID=fileInfo.getReceiverID();
            String path="C:\\Easy_message\\Upload\\"+this.senderID+"_To_"+this.receiverID;
            File file=new File(/*"C:\\Easy_message\\Upload\\"+this.senderID+"_To_"+this.receiverID*/path);
            if(!file.exists())
                file.mkdirs();
            path+="\\"+this.fileName;
            file=new File(path);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    System.out.println("创建文件!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] fileBytes=fileInfo.getFilebytes();

            BufferedOutputStream bos=null;
            try {
                bos=new BufferedOutputStream(new FileOutputStream(file,true));
                bos.write(fileBytes,0,fileBytes.length);

                bos.flush();
                bos.close();
                //System.out.println("写!");

                /**切掉这段会怎么样？**/
                /**dp.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
                ds.send(dp);**/
                //System.out.println("回发!");

                /*byte[] by=new byte[1024];
                dp = new DatagramPacket(by, 0, by.length);
                ds.get(dp);
                if (!UDPUtils.isEqualsByteArray(UDPUtils.successData, by, dp.getLength())) {
                    System.out.println("resend ...");
                    dp.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
                    ds.send(dp);
                }else {

                }*/
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static class MyTransThread implements Runnable{
        private String message;
        private DatagramSocket ds;
        private DatagramPacket dp;
        private String fileName;
        private String userID;
        public MyTransThread(String message, DatagramSocket ds,DatagramPacket dp) {
            this.message = message;
            this.ds = ds;
            this.dp=dp;
        }

        @Override
        public void run() {
            //System.out.println("收到!");
            message=message.substring(12,message.length());         //去掉报头
            //System.out.println("去掉头!");
            //System.out.println(message);
            MyFileInfo fileInfo=mygson.fromJson(message,mytype);          //获得对象
            this.fileName=fileInfo.getFileName();
            this.userID=fileInfo.getUserID();
            //System.out.println("fileName: "+fileName+"\tsenderID: "+senderID);
            String path="C:\\Easy_message\\Upload\\"+this.userID;
            File file=new File(/*"C:\\Easy_message\\Upload\\"+this.senderID+"_To_"+this.receiverID*/path);
            if(!file.exists())
                file.mkdirs();
            path+="\\"+this.fileName;
            file=new File(path);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    System.out.println("创建文件!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] fileBytes=fileInfo.getFilebytes();

            BufferedOutputStream bos=null;
            try {
                bos=new BufferedOutputStream(new FileOutputStream(file,true));
                bos.write(fileBytes,0,fileBytes.length);

                bos.flush();
                bos.close();
                //System.out.println("写!");
                dp.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
                ds.send(dp);
                //System.out.println("回发!");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
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

        @Override
        public void run() {
            //System.out.println("包长度: "+dp.getLength());
            message = new String(dp.getData(), 0, dp.getLength());
            //System.out.println("包内容: "+message.substring(0,10));
            if(message.startsWith("TransFile")){
                /**聊天中上传离线文件**/
                //System.out.println("进入分支!");
                TransThread transThread=new TransThread(message,ds,dp);
                Thread thread=new Thread(transThread);
                thread.start();
            }else if (message.startsWith("MyTransFile")){
                MyTransThread myTransThread=new MyTransThread(message,ds,dp);
                Thread thread=new Thread(myTransThread);
                thread.start();
            }else if (message.startsWith("Heartbeat")) {
                /**System.out.println("FileServer Touch...");**/
                //启动登录线程对登陆者执行检测地址操作
                HeartThread heartThread = new HeartThread(dp, ds);
                Thread thread = new Thread(heartThread);
                thread.start();
            } else if (message.startsWith("Login")) {
                /*try {
                    Thread.sleep(1000);                                             //保证客户端先出于receive状态?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                System.out.print("客户端文件系统上线!客户端本次登录地址:");
                //启动登录线程对登陆者执行一系列上线操作
                LoginThread loginThread = new LoginThread(dp, ds);
                Thread thread = new Thread(loginThread);
                thread.start();
            } /*else if (message.startsWith("SendFile")) {
                TransmitThread transmitThread = new TransmitThread(dp, ds);
                Thread thread = new Thread(transmitThread);
                thread.start();
            }*/
        }
    }

}

