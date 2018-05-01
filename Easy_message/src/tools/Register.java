package tools;

import connection.Conn;

import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

//实现注册功能
public class Register {
    public static void main(String[] args) throws SQLException {
        String nickName="juhkgf";
        String password="aqko251068";
        String phoneNum="17860536820";
        String newID=createNewID(nickName,password,phoneNum);
        createUserTable(newID.split(":")[1]);
        System.out.println("新ID:"+newID);
    }

    //指定号码长度，有最大长度限制
    private static final int ID_LENGTH = 10;

    private static String create_ID() {
        //随机生成10位数Easy_message号码
        String userID;
        do {
            Random dm = new Random();
            //Easy_message号码是两次生成的随机数拼接而成的字符串
            double theID_1 = dm.nextDouble() * Math.pow(10, ID_LENGTH / 2);
            double theID_2 = dm.nextDouble() * Math.pow(10, ID_LENGTH / 2);
            String theformer = String.valueOf(theID_1).substring(0, ID_LENGTH / 2);
            String thelatter = String.valueOf(theID_2).substring(0, ID_LENGTH / 2);
            if (thelatter.contains(".")) {
                thelatter = thelatter.substring(0, thelatter.indexOf("."));
                for (int i = 0; i < 5 - thelatter.length(); i++)
                    thelatter = "0" + thelatter;
            }
            userID = theformer + thelatter;
        } while (userID.contains("."));
        return userID;
    }

    //生成新的Easy_message号码
    public static String createNewID(String nickName, String passWord, String phoneNum) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "SELECT userID FROM userinfo";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        Set<String> ID_List = new HashSet<String>();
        //添加所有ID到集合ID_List中
        while (resultSet.next()) {
            ID_List.add(resultSet.getString("userID"));
        }

        //判断随机生成的号码是否与现有的重复
        boolean repetitive = true;
        boolean a = false;        //补全逻辑
        String newID = null;
        while (repetitive) {                                                                                      //一个逻辑坑
            newID = create_ID();
            Iterator<String> itr = ID_List.iterator();
            while (itr.hasNext()) {
                if (itr.next().equals(newID)) {
                    a = true;
                    break;
                }
            }
            if (!a)
                repetitive = false;
        }
        DateTime dateTime = new DateTime();
        Timestamp timestamp = dateTime.getCurrentDateTime();
        sql = "INSERT INTO userInfo(userID,nickName,passWord,phoneNum,exitTime) VALUES (?,?,?,?,?)";
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newID);
        preparedStatement.setString(2, nickName);
        preparedStatement.setString(3, passWord);

        preparedStatement.setString(4, phoneNum);
        preparedStatement.setTimestamp(5, timestamp);
        int is_Success = preparedStatement.executeUpdate();
        String result = is_Success + ":" + newID;
        Conn.Close();
        return result;
    }

    //生成5位邮箱验证码，验证码由5位字符组成，其中第2，5位为字母
    public static String createNewCode() {
        Random random = new Random();
        int thefirst = 'A';         //65-90
        int thelast = 'z';          //97-122
        int[] theInt = new int[3];
        for (int i = 0; i < 3; i++)
            theInt[i] = random.nextInt(10);
        int char_1 = 0;
        int char_2 = 0;
        boolean isAlphabet = false;
        while (!isAlphabet) {
            char_1 = random.nextInt(58) + 65;
            char_2 = random.nextInt(58) + 65;
            if ((char_1 > 90 && char_1 < 97) || (char_2 > 90 && char_2 < 97))
                continue;
            else
                isAlphabet = true;
        }
        String thecode = "" + theInt[0] + (char) char_1 + theInt[1] + theInt[2] + (char) char_2;
        return thecode;
       /*int[] thecode=new int[4];
       for(int i=0;i<4;i++){
           thecode[i]=random.nextInt(10);
       }
       String theCode = "";
       for(int i=0;i<4;i++){
           theCode=thecode[i]+theCode;
       }
       return theCode;*/
    }

    public static int createUserTable(String userID) throws SQLException {
        //批处理sql语句，以提高效率
        Connection connection = Conn.getConnection();
        connection.setAutoCommit(false);                                //取消自动提交
        int[] i = new int[0];
        try {
            String sql = "CREATE TABLE user_" + userID + "_Contactlist(ID varchar(11) not null comment '本user用户名' primary key ," +
                    " nickName varchar(255) not null comment '用户名/群名' , headIcon mediumblob null comment '用户头像/群头像' , types tinyint(1) unsigned not null comment '联系人类型(好友/群)' default 0 , " +
                    " status tinyint(1) unsigned not null comment '联系人状态(上下线)' default 0 , isupdate tinyint(1) unsigned not null comment '状态是否有更新' default 0)";
            //PreparedStatement preparedStatement = (PreparedStatement) connection.createStatement();
            Statement statement = connection.createStatement();
            statement.addBatch(sql);
            sql = "CREATE TABLE user_" + userID + "_ChatData(anotherID varchar(11) not null comment '另一用户的userID' , nature tinyint(1) not null comment '聊天性质(谁向谁发送信息;0代表我向他,1代表他向我,5代表我向他发图片,6代表他向我发图片)' " +
                    "default 1 , message text comment '聊天内容', img mediumblob comment '聊天图片(表情包)' , sendTime datetime not null comment '消息发送时间' , isAccepted char(1) default 'N' comment '(特指文件是否被同意接收)' )";
            statement.addBatch(sql);
            sql = "CREATE TABLE user_" + userID + "_NoticeList(anotherID varchar(11) not null comment '发送邀请的用户的userID或群ID' , nickName varchar(255) not null comment '群名或好友名' , property tinyint(10) not null comment '性质(群邀请或好友邀请等，默认0好友邀请)' default 0 )";
            statement.addBatch(sql);
            i = statement.executeBatch();                       //执行批处理语句
            connection.commit();                                        //如果没有异常则提交此段代码
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
        int result = 0;
        for (int j : i)
            result += j;
        //int result=preparedStatement.executeUpdate();
        //Conn.Close();
        return result;
    }
}
