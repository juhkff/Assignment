package tools;

import connection.Conn;

import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginAddressServer {

/*
    //设置全局变量map，存储客户端的端口键和IP值
    public static Map<String, String> map = new HashMap<String, String>();
*/

    //服务器收到的DatagramPacket中的数据是用户登录时使用的ID
    public static void main(String[] args) throws IOException, SQLException {
       /* int clientPort;                   //存储客户端端口
        String ip = "";                   //记录客户端ip地址*/

        //服务端端口为1111
        DatagramSocket ds = new DatagramSocket(1111);
        byte by[];
        //DatagramPacket dp = new DatagramPacket(by, 0, by.length);
        DatagramPacket dp;
        /*SocketAddress socketAddress = null;
        InetSocketAddress inetSocketAddress = null;
        String userId;                   //存储客户端地址*/
        while (true) {
            /*userId = "";*/
            by = new byte[512];
            dp = new DatagramPacket(by, 0, by.length);
            System.out.println("等待客户端上线/地址...");
            ds.receive(dp);
            LoginThread thread = new LoginThread(dp, ds);
            thread.start();
            /*System.out.println("客户端上线/地址获得!");
            userId = new String(dp.getData(), 0, dp.getLength());
            System.out.println("用户帐号：" + userId);
            socketAddress = dp.getSocketAddress();
            inetSocketAddress = (InetSocketAddress) socketAddress;
            clientPort = inetSocketAddress.getPort();             //获取客户端端口，每次登陆客户端的端口都不同
            ip = inetSocketAddress.getAddress().getHostAddress(); //获取客户端IP

            //地址格式：ip:端口
            String address = ip + ":" + clientPort;
            //map.put(String.valueOf(clientPort),ip);
            //将用户的ip和端口存储到数据库中
            Connection connection = Conn.getConnection();
            //查询该帐号对应的用户原IP地址是否和现IP地址相同
            String sql = "SELECT address FROM userinfo WHERE userId=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            String result = null;
            while (resultSet.next()) {
                i++;
                result = resultSet.getString("address");
            }
            if (i != 1) {
                System.out.println("数据查询错误！");
                throw new SQLException("数据查询异常，请排查原因...");
            } else {
                //该情况下已经准确获得该用户上一次（或刚才）登录的IP地址
                String former_ip = result.split(":")[0];

                //判断上次的登录ip与本次是否相同
                if (former_ip.equals(ip)) {
                    System.out.println("用户登录地址无变化!");
                    //当用户登录地址未发生变化时，返回succeed
                    by = "succeed".getBytes();
                    //dp.setData(by, 0, by.length);
                }
                //若是与上次的登录ip不同，则返回"update"字符串
                else {
                    sql = "UPDATE userinfo SET address=? WHERE userId=?";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, address);
                    preparedStatement.setString(2, userId);
                    int a = preparedStatement.executeUpdate();
                    if (a != 1) {
                        try {
                            throw new SQLException("获取地址失败！");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    by = "update".getBytes();              //设置回馈
                }
                dp.setData(by, 0, by.length);     //dp保留着发送地址
                ds.send(dp);                        //发送回馈
            }

            //客户端接受到字符串，则确认地址已经被添加.
            */
        }

    }
}

class LoginThread extends Thread {
    DatagramSocket ds;
    DatagramPacket dp;
    SocketAddress socketAddress = null;
    InetSocketAddress inetSocketAddress = null;
    String userId;                   //存储客户端地址
    int clientPort;                   //存储客户端端口
    String ip = "";                   //记录客户端ip地址

    public LoginThread(DatagramPacket dp, DatagramSocket ds) throws SocketException {
        this.dp = dp;
        this.ds = ds;
    }

    //@Override
    public void start() {
        super.start();
    }

    //@Override
    public void run() {
        System.out.println("客户端上线/地址获得!");
        userId = new String(dp.getData(), 0, dp.getLength());
        System.out.println("用户帐号：" + userId);
        socketAddress = dp.getSocketAddress();
        inetSocketAddress = (InetSocketAddress) socketAddress;
        clientPort = inetSocketAddress.getPort();             //获取客户端端口，每次登陆客户端的端口都不同
        ip = inetSocketAddress.getAddress().getHostAddress(); //获取客户端IP
        byte[] by;
        //地址格式：ip:端口
        String address = ip + ":" + clientPort;
        System.out.println("本次登录地址:" + address);
        //map.put(String.valueOf(clientPort),ip);
        //将用户的ip和端口存储到数据库中
        Connection connection = Conn.getConnection();
        //查询该帐号对应的用户原IP地址是否和现IP地址相同
        String sql = "SELECT address FROM userinfo WHERE userId=?";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            String result = null;
            while (resultSet.next()) {
                i++;
                result = resultSet.getString("address");
            }
            if (i != 1) {
                System.out.println("数据查询错误！");
                throw new SQLException("数据查询异常，请排查原因...");
            } else {
                //该情况下已经准确获得该用户上一次（或刚才）登录的IP地址
                String former_ip = result.split(":")[0];

                //判断上次的登录ip与本次是否相同
                if (former_ip.equals(ip)) {
                    System.out.println("用户登录地址无变化!");
                    //当用户登录地址未发生变化时，返回succeed
                    by = "succeed".getBytes();
                    //dp.setData(by, 0, by.length);
                    sql = "UPDATE userinfo SET address=? WHERE userId=?";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, address);
                    preparedStatement.setString(2, userId);
                    int a = preparedStatement.executeUpdate();
                    if (a != 1) {
                        try {
                            throw new SQLException("更新端口失败！");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        System.out.println("更新端口成功！");
                }
                //若是与上次的登录ip不同，则返回"update"字符串
                else {
                    sql = "UPDATE userinfo SET address=? WHERE userId=?";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, address);
                    preparedStatement.setString(2, userId);
                    int a = preparedStatement.executeUpdate();
                    if (a != 1) {
                        try {
                            throw new SQLException("获取地址失败！");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        System.out.print("更新地址成功！");
                    by = "update".getBytes();              //设置回馈
                }
                dp.setData(by, 0, by.length);     //dp保留着发送地址
                ds.send(dp);                        //发送回馈

                
               /* by="again".getBytes();
                SocketAddress socketAddress=dp.getSocketAddress();
                InetSocketAddress inetSocketAddress=(InetSocketAddress)socketAddress;
                String ip=inetSocketAddress.getAddress().getHostAddress();
                int port=inetSocketAddress.getPort();
                System.out.println("地址:"+ip+":"+port);
                dp=new DatagramPacket(by,0,by.length,new InetSocketAddress(ip,port));
                ds.send(dp);
*/
                System.out.println();                   //输出空行，使输出界面美观些
            }

            //客户端接受到字符串，则确认地址已经被添加.
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}