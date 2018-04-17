package tools;

import connection.Conn;

import java.sql.*;

public class Exit {
    public static void main(String[] args) throws SQLException {
        String userID="5627764619";
        changeStatus(userID);
    }
    public static int[] changeStatus(String userID) throws SQLException {
        //int result = 0;
        Connection connection = Conn.getConnection();
        connection.setAutoCommit(false);                                            /**手动提交**/
        String sql = "SELECT ID FROM user_" + userID + "_contactlist";            //查询自己好友列表，得到所有好友ID的集合
        int[] i = {-1, -1};
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String sql1;
            Statement statement1 = connection.createStatement();
            //Set<String> IDList=new HashSet<String>();
            while (resultSet.next()) {
                //IDList.add(resultSet.getString("ID"));
                sql1 = "UPDATE user_" + resultSet.getString("ID") + "_contactlist SET status=0 WHERE ID=" + userID;
                statement1.addBatch(sql1);
            }
            sql1="UPDATE userinfo SET isOnline=0 WHERE userID="+userID;
            statement1.addBatch(sql1);
            i = statement1.executeBatch();
            connection.commit();
        } finally {
            Conn.Close();
        }
        /*for (int j : i)
            result += j;
        if (result != 2) {
            try {
                throw new SQLException("用户退出时更新列表错误!Exit.java/changeStatus");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/
        return i;
    }

    public static int updateExitTime(String userID) throws SQLException {
        int result=0;
        Connection connection=Conn.getConnection();
        String sql="UPDATE userinfo SET exitTime=? WHERE userID=?";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        DateTime dateTime=new DateTime();
        preparedStatement.setTimestamp(1,dateTime.getCurrentDateTime());                                //数据库中如果是date而不是datetime则不能保存日期
        preparedStatement.setString(2,userID);
        result=preparedStatement.executeUpdate();
        Conn.Close();
        if(result!=1)
            throw new SQLException("更新时间出错!Exit.updateExitTime");
        else
            return result;
    }
}
