package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conn {
    private static final String URL = "jdbc:mysql://123.207.13.112:3306/easy_message?useSSL=false&autoReconnect=true&useUnicode=true&mysqlEncoding=utf8&characterEncoding=utf-8";
    private static final String USER = "root";
    private static final String PASSWORD = "7895123";
    private static Connection connection;

    public Conn() {
    }

    public static Connection getConnection() {
        connection = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static void Close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Connection connection = getConnection();
        if (connection == null)
            System.out.println("false");
        else
            System.out.println("succeed");
        Conn.Close();
    }
}
