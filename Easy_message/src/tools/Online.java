package tools;

import connection.Conn;
import model.User;
import test.Client.Request;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Online {
    public static void main(String[] args) {
        /*
        //测试scanner.nextLine()
        Scanner scanner=new Scanner(System.in);
        while(true){
            String ID=scanner.nextLine();
            System.out.println("界面输出:"+ID);
        }
        */

        String userID="8076357234";
        String receiverID="1005221246";
        String nickName="juhkgf";
        sendRequest(userID,nickName,receiverID);
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

    //监控并随时更新用户的本地局域网地址
    public static final int updateLocalAddress(String userID, String localAddress) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        return 0;
    }

    public static Map<String, String> getAddList(String userID) throws SQLException {
        Map<String,String> userList=new HashMap<String, String>();
        Connection connection=Conn.getConnection();
        String sql="SELECT userinfo.userID,userinfo.nickName FROM userinfo LEFT JOIN user_"+userID+"_contactlist ON userinfo.userID=user_"+userID+"_contactlist.ID IS NULL WHERE userinfo.userID!=\'"+userID+"\'";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        ResultSet resultSet=preparedStatement.executeQuery();
        while (resultSet.next()){
            userList.put(resultSet.getString("userID"),resultSet.getString("nickName"));
        }
        Conn.Close();
        return userList;
    }

    public final static String sendRequest(String userID,String nickName,String receiverID){
        String result = "";
        Connection connection=Conn.getConnection();
        String sql="SELECT anotherID,property FROM user_"+receiverID+"_noticelist WHERE anotherID=? AND property=0";
        try {
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,userID);
            ResultSet resultSet=preparedStatement.executeQuery();
            int i=0;
            while (resultSet.next()){
                if(i>1)
                    throw new SQLException("数据重复!Online.sendRequest");
                i++;
            }
            if(i!=0){
                result = "CF";        //重复
            }else {
                sql = "INSERT INTO user_" + receiverID + "_noticelist ( anotherID , nickName , property ) VALUES (?,?,?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, userID);
                preparedStatement.setString(2,nickName);
                if(userID.length()==10)
                    preparedStatement.setInt(3, 0);
                else if(userID.length()==6)
                    preparedStatement.setInt(2,1);
                else
                    throw new IOException("ID长度不对!Online.sendRequest");
                int j = preparedStatement.executeUpdate();
                if(j==1)
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


    /**根据userID找出用户的方法**/
    public static final User findUserByUserID(String userID) throws SQLException {
        Connection connection=Conn.getConnection();
        String sql="SELECT * FROM userinfo WHERE userID=?";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setString(1,userID);

        ResultSet resultSet=preparedStatement.executeQuery();
        int i=0;
        User user = null;
        while (resultSet.next()){
            i++;
            //String userID=resultSet.getString("userID");
            String nickName=resultSet.getString("nickName");
            boolean isMale=resultSet.getBoolean("isMale");
            String birthday=resultSet.getString("birthday");
            String email=resultSet.getString("enail");
            String phoneNUm=resultSet.getString("phoneNum");
            String exitTime=resultSet.getString("exitTime");
            user=new User(userID,nickName,isMale,birthday,email,phoneNUm,exitTime);
        }
        Conn.Close();
        return user;
    }
}
