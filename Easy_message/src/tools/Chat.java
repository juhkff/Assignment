package tools;

import connection.Conn;
import model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;

public class Chat {
    public final static ArrayList<ChatMessage> getChatHistoryList(String userID,String anotherID) throws SQLException {
        ArrayList<ChatMessage> chatMessages=new ArrayList<ChatMessage>();
        Connection connection= Conn.getConnection();
        String sql="SELECT * FROM user_"+userID+"_chatdata WHERE anotherID=? ORDER BY sendTime ASC";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setString(1,anotherID);
        ResultSet resultSet=preparedStatement.executeQuery();
        while (resultSet.next()){
            byte nature= (byte) resultSet.getInt("nature");
            String message=resultSet.getString("message");
            String sendTime= String.valueOf(resultSet.getTimestamp("sendTime"));
            ChatMessage chatMessage=new ChatMessage(anotherID,nature,sendTime,message);
            chatMessages.add(chatMessage);
        }
        return chatMessages;
    }

    public final static void insertChatMessage(String userID,String anotherID,String message,Timestamp sendTime) throws SQLException {
        try {
            Connection connection = Conn.getConnection();
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO user_" + userID + "_chatdata ( anotherID , nature , message , sendTime ) VALUES (" + anotherID + "," + 0 + "," + message + ",\'" + sendTime + "\')";
            statement.addBatch(sql);
            sql = "INSERT INTO user_" + anotherID + "_chatdata ( anotherID , nature , message , sendTime ) VALUES (" + userID + "," + 1 + "," + message + ",\'" + sendTime + "\')";
            statement.addBatch(sql);
            statement.executeBatch();
            connection.commit();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            Conn.Close();
        }
    }

    public final static String getReceiverAddress(String anotherID) {
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT remoteAddress FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,anotherID);
            ResultSet resultSet=preparedStatement.executeQuery();
            String address = null;
            while (resultSet.next()){
                address=resultSet.getString("remoteAddress");
            }
            return address;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }finally {
            Conn.Close();
        }
    }

    public static void updateContactStatus(String senderID, String anotherID) {
        try {
            Connection connection=Conn.getConnection();
            Statement statement=connection.createStatement();
            String sql;
            sql="UPDATE user_"+senderID+"_contactlist SET isupdate=1 WHERE ID="+anotherID;  //或者只需要在退出聊天窗口时调用这个方法?
            statement.addBatch(sql);
            sql="UPDATE user_"+anotherID+"_contactlist SET isupdate=1 WHERE ID="+senderID;
            statement.addBatch(sql);
            statement.executeBatch();
            connection.commit();
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
    }

    public static boolean checkOnlineStatus(String anotherID) {
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT isOnline FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,anotherID);
            ResultSet resultSet=preparedStatement.executeQuery();
            int status=-1;
            while (resultSet.next()){
                status=resultSet.getInt("isOnline");
            }
            boolean isOnline=status==1?true:false;
            return isOnline;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
        return false;
    }

    public final static String getLocalAddress(String userID){
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT localAddress FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,userID);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String address=resultSet.getString("localAddress");
                return address;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
        return null;
    }
}
