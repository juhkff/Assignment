package p2pserver.fileServerDao;

import connection.Conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Address {
    private static Connection connection = Conn.getConnection();
    private static String sql;
    private static PreparedStatement preparedStatement;

    public static final String getFileAddress(String userID) throws SQLException {
        String messageAddress = "";
        do {
            connection.setAutoCommit(true);
            sql = "SELECT remoteFileAddress FROM userinfo WHERE userID=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int num = 0;
            while (resultSet.next()) {
                num++;
                if (num != 1)
                    throw new SQLException("查询错误!fileServer.Address.getFileAddress");
                messageAddress = resultSet.getString("remoteFileAddress");
            }
        } while (messageAddress.equals(""));
        return messageAddress;
    }
}