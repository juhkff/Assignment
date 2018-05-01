package servlets.login;

import connection.Conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {
    public static void main(String[] args) {
        //将用户的(局域网和公网映射)地址存到数据库中
        Connection connection = Conn.getConnection();
        Statement statement = null;
        int[] result = {-1, -1};
        String localAddress = "asdasd";
        String userID = "6995965022";
        String address = "ddddddd";
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
    }
}
