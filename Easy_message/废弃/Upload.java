package tools;

import connection.Conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Upload {
    //判断上传的文件是否与服务器中已有文件内容重复
    public static Set<String> getMD5List(String userID) throws SQLException {
        Set<String> MD5_List = new HashSet<>();
        Connection connection = Conn.getConnection();
        String sql = "SELECT MD5 FROM " + userID + "_files";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            MD5_List.add(resultSet.getString("MD5"));
        }
        Conn.Close();
        return MD5_List;

    }

    //上传文件的数据库操作，暂未定
    public static void savePath(String oldPath, String newPath) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
    }
}
