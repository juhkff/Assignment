package tools;

import connection.Conn;
import model.contact.Contact;
import model.property.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Online {

    //辅助方法，获得用户已添加的好友ID列表
    public static final ResultSet getFriendIDResultSet(String userID,Connection connection){
        ResultSet resultSet = null;
        try {
            //Connection connection=Conn.getConnection();
            String sql="SELECT ID FROM user_"+userID+"_contactlist WHERE types=0";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            resultSet=preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Online.getFriendIDList !");
        } finally {
//            Conn.Close();
        }
        return resultSet;
    }

    //辅助方法，获得用户已添加的群ID列表
    public static final ResultSet getGroupIDResultSet(String userID,Connection connection){
        ResultSet resultSet=null;
        try {
            Connection connection1=Conn.getConnection();
            String sql="SELECT ID FROM user_"+userID+"_contactlist WHERE types=1";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            resultSet=preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Online.getGroupIDResultSet() !");
        } finally {
            Conn.Close();
        }
        return resultSet;
    }

    public static final String getMessageAddressByID(String userID) {
        String result = "error in tools.Online.getMessageAddressByID";
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT remoteAddress FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,userID);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                result=resultSet.getString("remoteAddress");
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }finally {
            Conn.Close();
        }
    }

    //查找并返回数据库中存储的用户的本地局域网地址
    public static final String getLocalAddress(String userID) throws SQLException {
        String result = "";
        do {
            Connection connection = Conn.getConnection();
            String sql = "SELECT localAddress FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int num = 0;
            while (resultSet.next()) {
                num++;
                if (num != 1)
                    throw new SQLException("查询出错!");
                result = resultSet.getString("localAddress");
            }
        } while (result.equals(""));
        Conn.Close();
        return result;
    }

    //添加好友而非群
    public static Map<String, Contact> getAddList(String theuserID) throws SQLException {
        Map<String, Contact> userList = new HashMap<String, Contact>();
        Connection connection = Conn.getConnection();
        String sql = "SELECT userinfo.userID,userinfo.nickName,userinfo.headIcon,userinfo.intro,userinfo.isMale,userinfo.isOnline FROM userinfo LEFT JOIN user_" + theuserID + "_contactlist ON userinfo.userID=user_" + theuserID + "_contactlist.ID  WHERE user_" + theuserID + "_contactlist.ID IS NULL AND userinfo.userID!=\'" + theuserID + "\'";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        String userID;
        String nickName;
        byte[] headIcon = null;
        String intro=null;
        Boolean isMale;
        Boolean isOnline;

        while (resultSet.next()) {
            userID=resultSet.getString("userID");
            nickName=resultSet.getString("nickName");
            InputStream inputStream=resultSet.getBinaryStream("headIcon");
            if (inputStream==null)
                headIcon=null;
            else{
                try {
                    headIcon=new byte[inputStream.available()];
                    inputStream.read(headIcon,0,inputStream.available());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("error in Online.getAddList()!");
                }
            }
            intro=resultSet.getString("intro");
            isMale=resultSet.getBoolean("isMale");
            isOnline=resultSet.getBoolean("isOnline");
            Contact contact=new Contact(userID,nickName,headIcon,intro,isMale, (byte) 0,isOnline);
            userList.put(userID,contact);
            //userList.put(resultSet.getString("userID"), resultSet.getString("nickName"));
        }
        Conn.Close();
        return userList;
    }


    public final static String sendRequest(String userID, String nickName, String receiverID) {
        String result = "";
        Connection connection = Conn.getConnection();
        String sql = "SELECT anotherID,property FROM user_" + receiverID + "_noticelist WHERE anotherID=? AND property=0";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                if (i > 1)
                    throw new SQLException("数据重复!Online.sendRequest");
                i++;
            }
            if (i != 0) {
                result = "CF";        //重复
            } else {
                sql = "INSERT INTO user_" + receiverID + "_noticelist ( anotherID , nickName , property ) VALUES (?,?,?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, userID);
                preparedStatement.setString(2, nickName);
                if (userID.length() == 10)
                    preparedStatement.setInt(3, 0);
                else if (userID.length() == 6)
                    preparedStatement.setInt(3, 1);
                else
                    throw new IOException("ID长度不对!Online.sendRequest");
                int j = preparedStatement.executeUpdate();
                if (j == 1)
                    result = "CG";    //成功
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
        return result;              //result:CF/CG
    }


    /**同意好友邀请**/
    public final static int sendAgreeResponse(String userID, String nickName, String receiverID) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "INSERT INTO user_" + receiverID + "_noticelist ( anotherID , nickName , property ) VALUES (?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userID);
        preparedStatement.setString(2, nickName);
        preparedStatement.setInt(3, 2);                      /**2:同意好友邀请**/
        int result = preparedStatement.executeUpdate();

        Conn.Close();
        return result;
    }


    /**根据userID找出用户的方法**/
    public static final User findUserByUserID(String userID) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "SELECT * FROM userinfo WHERE userID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userID);

        ResultSet resultSet = preparedStatement.executeQuery();
        int i = 0;
        User user = null;
        while (resultSet.next()) {
            i++;
            //String userID=resultSet.getString("userID");
            String nickName = resultSet.getString("nickName");
            boolean isMale = resultSet.getBoolean("isMale");
            String birthday = resultSet.getString("birthday");
            String email = resultSet.getString("enail");
            String phoneNUm = resultSet.getString("phoneNum");
            String exitTime = resultSet.getString("exitTime");
            user = new User(userID, nickName, isMale, birthday, email, phoneNUm, exitTime);
        }
        Conn.Close();
        return user;
    }

    /**添加好友**/
    public final static void bothAddFriend(String userID, String ID, String nickName) throws SQLException, IOException {
        Connection connection = Conn.getConnection();
        String sql = "SELECT headIcon,nickName,isOnline FROM userinfo WHERE userID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userID);
        ResultSet resultSet = preparedStatement.executeQuery();
        InputStream Agree_inputStream = null;
        String user_Name = null;
        while (resultSet.next()) {
            Agree_inputStream = resultSet.getBinaryStream("headIcon");
            user_Name = resultSet.getString("nickName");
        }
        InputStream Agreed_inputStream = null;
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, ID);
        resultSet = preparedStatement.executeQuery();
        boolean isOnline = false;
        while (resultSet.next()) {
            Agreed_inputStream = resultSet.getBinaryStream("headIcon");
            isOnline = resultSet.getInt("isOnline") != 0;
        }

        sql = "INSERT INTO user_" + userID + "_contactlist(ID,nickName,headIcon,types,status) VALUES (?,?,?,?,?)";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, ID);
        preparedStatement.setString(2, nickName);
        if (Agreed_inputStream != null)
            preparedStatement.setBinaryStream(3, Agreed_inputStream, Agreed_inputStream.available());
        else
            preparedStatement.setBinaryStream(3, null);
        preparedStatement.setInt(4, 0);
        preparedStatement.setBoolean(5, isOnline);
        int i = preparedStatement.executeUpdate();

        sql = "INSERT INTO user_" + ID + "_contactlist(ID,nickName,headIcon,types,status) VALUES (?,?,?,?,?)";
        PreparedStatement preparedStatement1 = connection.prepareStatement(sql);
        preparedStatement1.setString(1, userID);
        preparedStatement1.setString(2, user_Name);
        if (Agree_inputStream != null)
            preparedStatement1.setBinaryStream(3, Agree_inputStream, Agree_inputStream.available());
        else
            preparedStatement1.setBinaryStream(3, null);
        preparedStatement1.setInt(4, 0);
        preparedStatement1.setBoolean(5, true);
        int j = preparedStatement1.executeUpdate();
        Conn.Close();
    }

    public static String commitUserInfo(User user) {
        String result=null;
        InputStream inputStream=null;
        inputStream=new ByteArrayInputStream(user.getHeadIcon());
        try {
            Connection connection=Conn.getConnection();
            String sql="UPDATE userinfo SET nickName=?,passWord=?,headIcon=?,isMale=?,birthday=?,email=?,phoneNum=?,intro=? WHERE userID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getNickName());
            preparedStatement.setString(2,user.getPassWord());
//            InputStream inputStream=null;
//            inputStream=new ByteArrayInputStream(user.getHeadIcon());
            preparedStatement.setBinaryStream(3,inputStream);
            preparedStatement.setBoolean(4,user.isMale());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(user.getBirthday()));
            preparedStatement.setString(6,user.getEmail());
            preparedStatement.setString(7,user.getPhoneNum());
            preparedStatement.setString(8,user.getIntro());
            preparedStatement.setString(9,user.getUserID());
            int commitResult=preparedStatement.executeUpdate();

            ResultSet friendIDResultSetresultSet=Online.getFriendIDResultSet(user.getUserID(),connection);
            while (friendIDResultSetresultSet.next()) {
                String ID=friendIDResultSetresultSet.getString("ID");
                String sql1 = "UPDATE user_"+ID+"_contactlist SET nickName=?,headIcon=?,isupdate=1 WHERE ID=?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement1.setString(1,user.getNickName());
//                InputStream inputStream1=null;
//                inputStream1 = new ByteArrayInputStream(user.getHeadIcon());
                preparedStatement1.setBinaryStream(2,inputStream);
                preparedStatement1.setString(3,user.getUserID());
                int commitResult1=preparedStatement1.executeUpdate();
            }
            ResultSet groupIDResultSet=Online.getGroupIDResultSet(user.getUserID(),connection);


            while (groupIDResultSet.next()){
                String ID=groupIDResultSet.getString("ID");
                String sql2="UPDATE group_"+ID+"_member SET userName=?,userHeadIcon=? WHERE userID=?";
                PreparedStatement preparedStatement2=connection.prepareStatement(sql2);
                preparedStatement2.setString(1,user.getNickName());
                preparedStatement2.setBinaryStream(2,inputStream);
                preparedStatement2.setString(3,user.getUserID());
                int commitResult2=preparedStatement2.executeUpdate();
            }
            result="success";
        } catch (SQLException e) {
            e.printStackTrace();
            result="error in Tools.Online.commitUserInfo";
        } finally {
            Conn.Close();
        }
        return result;
    }
}
