package tools;

import connection.Conn;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class Exit {

    public static int[] changeStatus(String userID) throws SQLException {
        //int result = 0;
        Connection connection = Conn.getConnection();
        connection.setAutoCommit(false);                                            /**手动提交**/
        String sql = "SELECT ID FROM user_" + userID + "_contactlist WHERE types=0";            //查询自己好友列表，得到所有好友ID的集合
        int[] i = {-1, -1};
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String sql1;
            Statement statement1 = connection.createStatement();
            //Set<String> IDList=new HashSet<String>();
            while (resultSet.next()) {
                //IDList.add(resultSet.getString("ID"));
                sql1 = "UPDATE user_" + resultSet.getString("ID") + "_contactlist SET status=0,isupdate=1 WHERE ID=" + userID;
                statement1.addBatch(sql1);
            }
            sql1 = "UPDATE userinfo SET isOnline=0 WHERE userID=" + userID;
            statement1.addBatch(sql1);
            i = statement1.executeBatch();
            connection.commit();
        } finally {
            Conn.Close();
        }
        return i;
    }

    public static int updateExitTime(String userID) throws SQLException {
        int result = 0;
        Connection connection = Conn.getConnection();
        String sql = "UPDATE userinfo SET exitTime=? WHERE userID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        DateTime dateTime = new DateTime();
        preparedStatement.setTimestamp(1, dateTime.getCurrentDateTime());                                //数据库中如果是date而不是datetime则不能保存日期
        preparedStatement.setString(2, userID);
        result = preparedStatement.executeUpdate();
        Conn.Close();
        if (result != 1)
            throw new SQLException("更新时间出错!Exit.updateExitTime");
        else
            return result;
    }

    public static void changeGroupStatus(String userID) throws SQLException {
        Connection connection = Conn.getConnection();
        connection.setAutoCommit(false);                                            /**手动提交**/
        String sql = "SELECT ID FROM user_" + userID + "_contactlist WHERE types=1";            //查询自己群列表，得到所有群ID的集合
        int[] i;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String sql1;
            Statement statement1 = connection.createStatement();
            //Set<String> IDList=new HashSet<String>();
            while (resultSet.next()) {
                //IDList.add(resultSet.getString("ID"));
                sql1 = "UPDATE group_" + resultSet.getString("ID") + "_member  SET userOnline=0,isUpdate=1 WHERE userID=" + userID;
                statement1.addBatch(sql1);
            }
            sql1 = "UPDATE userinfo SET isOnline=0 WHERE userID=" + userID;
            statement1.addBatch(sql1);
            i = statement1.executeBatch();
            connection.commit();
        } finally {
            Conn.Close();
        }
    }
}
