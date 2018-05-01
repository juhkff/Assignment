package tools.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import connection.Conn;
import model.FileMessage;
import tools.DateTime;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;

public class File {

    public final static void upLoadNewFile(String userID, String anotherID, String fileName) {
        /**fileName是文件全路径**/
        try {
            Timestamp sendTime = new DateTime().getCurrentDateTime();
            Connection connection = Conn.getConnection();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO user_" + userID + "_chatdata ( anotherID , nature , message , sendTime ) VALUES (" + anotherID + "," + 2 + ",\'" + fileName + "\',\'" + sendTime + "\')";
            statement.addBatch(sql);
            sql = "INSERT INTO user_" + anotherID + "_chatdata ( anotherID , nature , message , sendTime ) VALUES (" + userID + "," + 3 + ",\'" + fileName + "\',\'" + sendTime + "\')";
            statement.addBatch(sql);
            statement.executeBatch();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
    }

    // 判断一个字符是否是中文
    public final static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    // 判断一个字符串是否含有中文
    public final static boolean isChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) return true;// 有一个中文字符就返回
        }
        return false;
    }

    public final static String checkRequest(String userID,String anotherID,String fileName) throws Exception {
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT isAccepted FROM user_"+userID+"_chatdata WHERE message='"+fileName+"'";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            ResultSet resultSet=preparedStatement.executeQuery();
            String isAccepted="";
            while (resultSet.next()){
                isAccepted=resultSet.getString("isAccepted");
            }
            return isAccepted;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("error!");
        } finally {
            Conn.Close();
        }
    }

    public static final void insertFileMessage(String userID, String anotherID, String message, Timestamp sendTime) {
        try {
            System.out.println("文件名1:"+message);
            Connection connection = Conn.getConnection();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO user_" + userID + "_chatdata ( anotherID , nature , message , sendTime , isAccepted ) VALUES (" + anotherID + "," + 0 + ",\'" + message + "\',\'" + sendTime + "\',\'N\')";
            statement.addBatch(sql);
            sql = "INSERT INTO user_" + anotherID + "_chatdata ( anotherID , nature , message , sendTime ) VALUES (" + userID + "," + 1 + ",\'" + message + "\',\'" + sendTime + "\')";
            statement.addBatch(sql);
            statement.executeBatch();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
    }

    /**根据消息地址得到相应文件地址**/
    public static String getFileAddress(String req) throws SQLException {
        String response;
        try {
            String senderAddr = req.split("/")[0];
            String receiverAddr = req.split("/")[1];
            String senderFileAddr = "error(getFileAddress_senderAddr)";
            String receiverFileAddr = "error(getFileAddress_receiverAddr)";
            Connection connection = Conn.getConnection();
            String sql = "SELECT localFileAddress From userinfo WHERE localAddress LIKE '%" + senderAddr + "%'";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                senderFileAddr = resultSet.getString("localFileAddress");
            }
            sql = "SELECT localFileAddress From userinfo WHERE localAddress LIKE '%" + receiverAddr + "%'";
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                receiverFileAddr = resultSet.getString("localFileAddress");
            }
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();

            ArrayList<String> senderFileAddrList=gson.fromJson(senderFileAddr,type);
            ArrayList<String> receiverFileAddrList=gson.fromJson(receiverFileAddr,type);
            for(String each:senderFileAddrList){
                if(each.indexOf(senderAddr.split(",")[0])>=0)
                    senderAddr=each;
            }
            for(String each:receiverFileAddrList){
                if(each.indexOf(receiverAddr.split(",")[0])>=0)
                    receiverAddr=each;
            }
            response = senderAddr + "/" + receiverAddr;
        }finally {
            Conn.Close();
        }
        return response;
    }

    public static String updateFileStatus(String senderID, String fileName) {
        String result = "error in tools.File.undateFileStatus";
        try {
            System.out.println("文件名2:"+fileName);
            Connection connection=Conn.getConnection();
            String sql="UPDATE user_"+senderID+"_chatdata SET isAccepted='T' WHERE message='"+fileName+"'";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
//            preparedStatement.setString(1,fileName);
            int i=preparedStatement.executeUpdate();
            System.out.println(i);
            result="success";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
