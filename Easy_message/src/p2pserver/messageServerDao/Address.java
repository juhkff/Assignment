package p2pserver.messageServerDao;

import connection.Conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Address {
    private static Connection connection = Conn.getConnection();
    private static String sql;
    private static PreparedStatement preparedStatement;

    public static final String getMessageAddress(String userID) throws SQLException {
        String messageAddress = "";
        do {
            connection.setAutoCommit(true);
            sql = "SELECT remoteAddress FROM userinfo WHERE userID=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int num = 0;
            while (resultSet.next()) {
                num++;
                if (num != 1)
                    throw new SQLException("查询错误!messageServer.Address.getMessageAddress");
                messageAddress = resultSet.getString("remoteAddress");
            }
        } while (messageAddress.equals(""));
        return messageAddress;
    }
}
