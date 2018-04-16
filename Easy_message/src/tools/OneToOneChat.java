package tools;

import connection.Conn;

import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OneToOneChat {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(2222);
        DatagramPacket dp;
        byte by[];
        while (true) {
            by = new byte[2048];                                //（划）暂时不这样写，以防多线程共同用一个byte数组
            dp = new DatagramPacket(by, 0, by.length);
            System.out.println("等待接受消息...");
            ds.receive(dp);
            String content=new String(dp.getData(),0,dp.getLength());
            if(new String(dp.getData(),0,dp.getLength()).equals("")){

            }
                //System.out.println("收到:"+new String(dp.getData(),0,dp.getLength()));
            else if(new String(dp.getData(),0,dp.getLength()).startsWith("Connect")){
                System.out.println(new String(dp.getData(),0,dp.getLength()));
            }
            else if(new String(dp.getData(),0,dp.getLength()).startsWith("Send")){
                SenderThread thread = new SenderThread(dp, ds);
                thread.start();
            }
            else /*if(new String(dp.getData(),0,dp.getLength()).startsWith("punch"))*/{
                System.out.println("发送消息打洞成功!");
                dp.setData("success".getBytes());
                SocketAddress socketAddress=dp.getSocketAddress();
                InetSocketAddress inetSocketAddress= (InetSocketAddress) socketAddress;
                String IP=inetSocketAddress.getAddress().getHostAddress();
                int PORT=inetSocketAddress.getPort();
                String address=IP+":"+PORT;
                System.out.println("本次发送punch的地址来源: "+content+"    地址: "+address);
                ds.send(dp);

                Connection connection=Conn.getConnection();
                String sql="UPDATE userinfo SET address=? WHERE userID=?";
                try {
                    PreparedStatement preparedStatement=connection.prepareStatement(sql);
                    preparedStatement.setString(1,address);
                    preparedStatement.setString(2,content);
                    int result=preparedStatement.executeUpdate();
                    System.out.println("OTOChat更新用户地址返回值："+result);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            //dp.setData(by, 0, by.length);
        }


    /*    Connection connection=Conn.getConnection();
        String sql="SELECT address FROM userinfo WHERE userID=?";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        ResultSet resultSet=preparedStatement.executeQuery();
        int i=0;
        String address;
        while(resultSet.next()){
            SenderThread thread=new SenderThread(dp);

        }*/
    }
}

class SenderThread extends Thread {
    private DatagramSocket ds;                 //能否随机指定一个端口？值得一试
    private DatagramPacket dp;

    public SenderThread(DatagramPacket dp, DatagramSocket ds) {
        this.dp = dp;
        this.ds = ds;

    }

    //@Override
    /*public void start() {
        super.start();
    }*/

    //发送方的地址没有用到或被发送
    //@Override
    public void run() {
        //接受dp包发送的数据
        SocketAddress receive_address;
        String message = new String(dp.getData(), 0, dp.getLength());
        System.out.println("成功接受信息！message:" + message);
        SocketAddress socketAddress = dp.getSocketAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        dp.setData("getThis".getBytes());
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //String sendIP=inetSocketAddress.getAddress().getHostAddress();

        //下一步：寻找发送到的帐号，取出ip并发送
        if (message.startsWith("Send")) {                     //数据报需要以Send开头
            //接收方的帐号
            String infomation = message.substring(0, 26);
            String senderId = infomation.substring(5, 15);
            System.out.println("数据来自于:" + senderId);   //提示发送方的Id
            String receiverId = infomation.substring(16, 26);
            System.out.println("数据发送给:" + receiverId);  //提示接收方的Id
            message = senderId + ":" + message.substring(27, message.length());     //获得发送方Id和聊天信息                可能会出错


            Connection connection = Conn.getConnection();
            String sql = "SELECT address FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement;
            String receiverAddress = null;
            try {
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, receiverId);                  //查询接收方的IP地址
                ResultSet resultSet = preparedStatement.executeQuery();
                int result = 0;
                while (resultSet.next()) {
                    result++;
                    //接收方的地址
                    receiverAddress = resultSet.getString("address");


                    System.out.println("接收方的地址:"+receiverAddress);
                }
                if (result != 1)
                    throw new SQLException("有多个重复帐号！");
                if (receiverAddress == null)
                    throw new SQLException("找不到接收方地址！");
                else {
                    //记录接收端的ip
                    String receiverIp = receiverAddress.split(":")[0];
                    //接收方的端口
                    String receiverPort = receiverAddress.split(":")[1];
                    //发送数据的字节流
                    byte[] messageBuf = message.getBytes();
                    receive_address = new InetSocketAddress(receiverIp, Integer.parseInt(receiverPort));
                    dp = new DatagramPacket(messageBuf, 0, messageBuf.length, receive_address);
                    dp.setData(messageBuf,0,messageBuf.length);


                    System.out.println("发送方地址:"+inetSocketAddress.getAddress().getHostAddress()+":"+inetSocketAddress.getPort());
                    ds.send(dp);
                    System.out.print("转发代码执行完毕！");
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                throw new Exception("数据发送格式错误:" + message.substring(0, 26));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println();                       //输出空行，使输出界面美观些
    }

}
