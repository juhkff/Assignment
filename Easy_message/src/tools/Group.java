package tools;

import connection.Conn;
import model.group.GroupMember;
import model.group.GroupMessage;
import model.group.SimpleGroup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class Group {
    public static void main(String[] args) throws SQLException {
        /*String newGroupID=createNewGroupID();
        System.out.println(newGroupID);*/
        /*String newGroupID="123456";
        String groupName="test";
        String creatorID="1234567890";
        String createTime= String.valueOf(new DateTime().getCurrentDateTime());
        String sql="INSERT INTO groups(groupID, groupName, creatorID, managerNumber, groupNumber, createTime) " +
                "VALUES ("+newGroupID+",\'"+groupName+"\',"+creatorID+",0 ,1 ,\'"+createTime+"\' )";
        System.out.println(sql);*/
        String creatorID = "4422";
        String creatorName = "test";
        byte[] creatorHeadIcon = null;
        Connection connection = Conn.getConnection();
        String sql;
        sql = "INSERT INTO group_member_model(userID, userName, userHeadIcon, userStatus) VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, creatorID);
        preparedStatement.setString(2, creatorName);
        InputStream inputStream = null;
        if (creatorHeadIcon != null)
            inputStream = new ByteArrayInputStream(creatorHeadIcon);
        preparedStatement.setBinaryStream(3, inputStream);
        preparedStatement.setInt(4, 2);
        preparedStatement.execute();
    }

    private static final int ID_LENGTH = 6;


    //获取不重复的所有群(用于添加群)
    public static final ArrayList<SimpleGroup> showGroupList(String userID) {
        //String result = "tools\\Group.java just start";
        ArrayList<SimpleGroup> groupArrayList = new ArrayList<SimpleGroup>();
        try {
            Connection connection = Conn.getConnection();
            String sql = "SELECT * FROM groups WHERE groupID NOT IN ( SELECT ID FROM user_" + userID + "_contactlist WHERE types=1 )";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            String groupID;
            String groupName;
            //String groupIntro;
            byte[] groupIcon;
            while (resultSet.next()) {
                groupID = resultSet.getString("groupID");
                groupName = resultSet.getString("groupName");
                groupIcon = null;
                InputStream inputStream = resultSet.getBinaryStream("groupIcon");
                if (inputStream != null) {
                    groupIcon = new byte[inputStream.available()];
                    inputStream.read(groupIcon, 0, inputStream.available());
                }
                SimpleGroup group = new SimpleGroup(groupID, groupName, groupIcon);
                groupArrayList.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in tools\\Group.java");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in tools\\Group.java when new byte[inputStream.available]");
        } finally {
            Conn.Close();
        }
        return groupArrayList;
    }

    //生成新的不重复的群号码
    public static final String createNewGroupID() throws SQLException {
        String groupID = null;
        Connection connection = Conn.getConnection();
        String sql = "SELECT groupID FROM groups";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        Set<String> ID_List = new HashSet<String>();
        //添加所有ID到集合ID_List中
        while (resultSet.next()) {
            ID_List.add(resultSet.getString("groupID"));
        }

        //判断随机生成的号码是否与现有的重复
        boolean repetitive = true;
        boolean a = false;        //补全逻辑
        String newID = null;
        while (repetitive) {                                                                                      //一个逻辑坑
            newID = createGroupID();
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
        return newID;
    }

    //随机生成6位数Easy_message号码
    private static String createGroupID() {
        String groupID;
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
            groupID = theformer + thelatter;
        } while (groupID.contains("."));
        return groupID;
    }

    //创建新群
    public static final String createNewGroup(String groupName, String creatorID, String creatorName, byte[] creatorHeadIcon, Timestamp createTime) throws SQLException {
        String newGroupID = createNewGroupID();
        Connection connection = Conn.getConnection();
        connection.setAutoCommit(false);
        String sql;
        Statement statement = connection.createStatement();
        try {
            sql = "INSERT INTO groups(groupID, groupName, creatorID, managerNumber, groupNumber, createTime) " +
                    "VALUES (" + newGroupID + ",\'" + groupName + "\'," + creatorID + ",0 ,1 ,\'" + createTime + "\' )";
            statement.addBatch(sql);
            sql = "CREATE TABLE group_" + newGroupID + "_chatdata( senderID char(10) not null comment '发送者ID' , senderName varchar(255) not null comment '发送者昵称' , " +
//                    "senderHeadIcon mediumblob comment '发送者头像'"+
                    "sendTime datetime not null comment '发送时间' , Status tinyint(4) not null comment '发送属性(聊天、文件等)' default 0 , " +
                    "Content text comment '消息内容(聊天、文件等)' , Img mediumblob null comment '群聊图片' ,ifSuccess tinyint(3) not null comment '用来应对某种肯呢个出现的的特殊情况' default  0)";
            statement.addBatch(sql);
            sql = "CREATE TABLE group_" + newGroupID + "_notice(creatorID varchar(10) null," +
                    "  mentionedID varchar(10) null," +
                    "  status tinyint(2) unsigned default '0' not null," +
                    "  time datetime not null )";
            statement.addBatch(sql);
            sql = "create table group_" + newGroupID + "_member" +
                    "(" +
                    "  userID char(10) not null" +
                    "  primary key," +
                    "  userName varchar(255) not null," +
                    "  userHeadIcon mediumblob null," +
                    "  userStatus tinyint(2) default '0' not null comment '用户上下线,0下线,1上线' " +
                    "  comment '0为普通成员，1为管理员，2为群主'" +
                    ")";
            statement.addBatch(sql);
            InputStream inputStream = null;
            if (creatorHeadIcon != null)
                inputStream = new ByteArrayInputStream(creatorHeadIcon);

            sql="INSERT INTO group_" + newGroupID + "_member(userID, userName, userHeadIcon, userStatus) VALUES ("+creatorID+",\'"+creatorName+"\',"+inputStream+","+2+")";
            statement.addBatch(sql);
            sql="INSERT INTO user_"+creatorID+"_contactlist(ID,nickName,types) VALUES ("+newGroupID+",\'"+groupName+"\',"+1+")";
            statement.addBatch(sql);
            int[] result = statement.executeBatch();
            connection.commit();

//            Connection connection1=Conn.getConnection();
//            String sql1 = "INSERT INTO group_" + newGroupID + "_member(userID, userName, userHeadIcon, userStatus) VALUES (?,?,?,?)";
//            PreparedStatement preparedStatement = connection1.prepareStatement(sql1);
//            preparedStatement.setString(1, creatorID);
//            preparedStatement.setString(2, creatorName);
//            InputStream inputStream = null;
//            if (creatorHeadIcon != null)
//                inputStream = new ByteArrayInputStream(creatorHeadIcon);
//            preparedStatement.setBinaryStream(3, inputStream);
//            preparedStatement.setInt(4, 2);
//            int result1 = preparedStatement.executeUpdate();
        } finally {
            Conn.Close();
        }
        return newGroupID;
    }

    //加入群
    public static void joinGroup(String userID, String userName, byte[] userHeadIcon, String groupID) {
        try {
            Connection connection = Conn.getConnection();
            String sql = "INSERT INTO group_" + groupID + "_member(userID,userName,userHeadIcon,userStatus) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, userName);
            InputStream inputStream = null;
            if (userHeadIcon != null)
                inputStream = new ByteArrayInputStream(userHeadIcon);
            preparedStatement.setBinaryStream(3, inputStream);
            preparedStatement.setInt(4, 0);
            int result = preparedStatement.executeUpdate();

            /**将群添加到用户的联系人(群)列表中**/
            sql="INSERT INTO user_"+userID+"_contactlist(ID,nickName,headIcon,types) VALUES (?,?,?,?)";
            preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,groupID);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.joinGroup!");
        } finally {
            Conn.Close();
        }
    }

    //根据ID获得群对象
    public final static model.group.Group getGroup(String theGroupID,String URL_ADDRESS){
        model.group.Group group = null;
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT * FROM groups WHERE  groupID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,theGroupID);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String groupID=resultSet.getString("groupID");
                String groupName=resultSet.getString("groupName");
                String groupIntro=resultSet.getString("groupIntro");
                InputStream inputStream=null;
                byte[] groupIcon=null;
                inputStream=resultSet.getBinaryStream("groupIcon");
                if(inputStream!=null){
                    groupIcon=new byte[inputStream.available()];
                    inputStream.read(groupIcon,0,inputStream.available());
                    inputStream.close();
                }
                String creatorID=resultSet.getString("creatorID");
                int managerNumber=resultSet.getInt("managerNumber");
                int groupNumber=resultSet.getInt("groupNumber");
                String theLatestText=resultSet.getString("theLatestText");
                String theLatestTextTime=resultSet.getString("theLatestTextTime");
                String createTime=resultSet.getString("createTime");
                group=new model.group.Group(URL_ADDRESS,groupID,groupName,groupIcon,theLatestText,theLatestTextTime,groupIntro,creatorID,managerNumber,groupNumber,createTime);
                break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getGroup() !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.getGroup() when inputStream.available() !");
        } finally {
            Conn.Close();
        }
        return group;
    }

    //获得一个群所有的用户帐号查询结果集合
    public static ResultSet getMemberIDList(String groupID){
        ResultSet resultSet = null;
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT userID FROM group_"+groupID+"_member";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            resultSet=preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getMemberIDList() !");
        } finally {
//            Conn.Close();
        }
        return resultSet;
    }

    //用户添加群时获得的(单个)群对象
    public final static SimpleGroup getNewGroup(String theGroupID) throws IOException {
        SimpleGroup simpleGroup=null;
        InputStream inputStream=null;
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT groupID,groupName,groupIcon,theLatestText,theLatestTextTime FROM groups WHERE groupID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,theGroupID);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String groupID=resultSet.getString("groupID");
                String groupName=resultSet.getString("groupName");
                byte[] groupIcon=null;
                inputStream=resultSet.getBinaryStream("groupIcon");
                if(inputStream!=null){
                    groupIcon=new byte[inputStream.available()];
                    inputStream.read(groupIcon,0,inputStream.available());
                }
                String theLatestText=resultSet.getString("theLatestText");
                String theLatestTextTime=resultSet.getString("theLatestTextTime");

                simpleGroup=new SimpleGroup(groupID,groupName,groupIcon,theLatestText,theLatestTextTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
//            System.out.println("error in Group.getNewGroup() when connection.setAutoCommit(false) !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.getNewGroup() when inputStream.available() !");
        } finally {
            if (inputStream!=null)
                inputStream.close();
            Conn.Close();
        }
        return simpleGroup;
    }

    //用户登录时获取群列表(不含最新消息及其发送时间)
    public static Map<String, SimpleGroup> getGroupList(String userID) {
        Map<String, SimpleGroup> simpleGroupMap = new HashMap<String, SimpleGroup>();
        try {
            Connection connection = Conn.getConnection();
            String sql = "SELECT * FROM user_" + userID + "_contactlist WHERE types=1";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            String ID;
            String nickName;
            byte[] headIcon=null;
            InputStream inputStream=null;
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ID = resultSet.getString("ID");
                nickName = resultSet.getString("nickName");
                headIcon = null;
                inputStream = null;
                inputStream = resultSet.getBinaryStream("headIcon");
                if (inputStream != null) {
                    headIcon = new byte[inputStream.available()];
                    inputStream.read(headIcon, 0, inputStream.available());
                }
                SimpleGroup simpleGroup = new SimpleGroup(ID, nickName, headIcon);
                simpleGroupMap.put(ID, simpleGroup);
            }
            if(inputStream!=null)
                inputStream.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getGroupList!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.getGroupList when inputStream.available!");
        } finally {
            Conn.Close();
        }
        return simpleGroupMap;
    }

    //用户登录时获取群列表(包含最新消息及其发送时间)
    public static Map<String, SimpleGroup> getFullGroupList(String userID, Map<String, SimpleGroup> simpleGroupMap) {
        Map<String, SimpleGroup> stringSimpleGroupMap = simpleGroupMap;
        try {
            /**Connection其实可以和上一个方法的合并，因为二者是一起操作的**/
            Connection connection = Conn.getConnection();
            String sql1 = "SELECT groupID,theLatestText,theLatestTextTime FROM groups WHERE groupID IN (SELECT ID FROM user_" + userID + "_contactlist WHERE types=1) ";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            ResultSet resultSet = preparedStatement1.executeQuery();
            String groupID;
            String theLatestText;
            String theLatestTextTime;
            while (resultSet.next()) {
                groupID = resultSet.getString("groupID");
                theLatestText = resultSet.getString("theLatestText");
                theLatestTextTime = resultSet.getString("theLatestTextTime");

                /**引用变量会影响方法的形参对应的实参变量**/
                stringSimpleGroupMap.get(groupID).setTheLatestMessage(theLatestText);
                stringSimpleGroupMap.get(groupID).setTheLatestSendTime(theLatestTextTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getFullGroupList when connection.prepareStatement !");
        } finally {
            Conn.Close();
        }
        return stringSimpleGroupMap;
    }

/*    //群主身份验证
    public final static boolean isCreator(String userID,String groupID){
        try {
            Connection connection=Conn.getConnection();
            String sql="";
            PreparedStatement preparedStatement=connection.prepareStatement()
        }finally {
            Conn.Close();
        }
    }
*/

    //群主身份验证
    public final static boolean isCreator(GroupMember groupMember, String groupID) {
        if (groupMember.getUserStatus() == 2)
            return true;
        else
            return false;
    }

    /**
     * 获得群成员集合
     **/
    public static Map<String, GroupMember> getGroupMemberList(String groupID) {
        Map<String, GroupMember> groupMemberList = new HashMap<String, GroupMember>();
        try {
            Connection connection = Conn.getConnection();
            String sql = "SELECT * FROM group_" + groupID + "_member";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            String userID;
            String userName;
            byte[] userHeadIcon;
            byte userStatus;
            InputStream inputStream;
            while (resultSet.next()) {
                userID = resultSet.getString("userID");
                userName = resultSet.getString("userName");
                userHeadIcon = null;
                inputStream = null;
                inputStream = resultSet.getBinaryStream("userHeadIcon");
                if (inputStream != null) {
                    userHeadIcon = new byte[inputStream.available()];
                    inputStream.read(userHeadIcon, 0, inputStream.available());
                    inputStream.close();
                }
                userStatus = (byte) resultSet.getInt("userStatus");

                GroupMember groupMember = new GroupMember(userID, userName, userHeadIcon, userStatus);
                groupMemberList.put(userID, groupMember);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getGroupMemberList() !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.getGroupMemberList() when inputStream.available !");
        } finally {
            Conn.Close();
        }
        return groupMemberList;
    }

    //获得具体群信息
    public static model.group.Group getFullGroup(String url_address, String groupID, String groupName, byte[] groupIcon, String theLatestMessage, String theLatestSendTime) {
        model.group.Group group = null;

        try {
            Connection connection = Conn.getConnection();
            String sql = "SELECT groupIntro,creatorID,managerNumber,groupNumber,createTime FROM groups WHERE groupID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, groupID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String groupIntro = resultSet.getString("groupIntro");
                String creatorID = resultSet.getString("creatorID");
                int managerNumber = resultSet.getInt("managerNumber");
                int groupNumber = resultSet.getInt("groupNumber");
                String creatTime = String.valueOf(resultSet.getTimestamp("createTime"));
                group = new model.group.Group(url_address, groupID, groupName, groupIcon, theLatestMessage, theLatestSendTime,
                        groupIntro, creatorID, managerNumber, groupNumber, creatTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getFullGroup() !");
        } finally {
            Conn.Close();
        }

        return group;
    }

    public static ArrayList<GroupMessage> getChat(String groupID,String exitTime) {
        ArrayList<GroupMessage> groupMessages=new ArrayList<GroupMessage>();
        try {
            Timestamp timestamp= Timestamp.valueOf(exitTime);
            Connection connection=Conn.getConnection();
            String sql="SELECT * FROM group_"+groupID+"_chatdata WHERE sendTime>=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1,timestamp);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String senderID=resultSet.getString("senderID");
                String senderName=resultSet.getString("senderName");
                String sendTime= String.valueOf(resultSet.getTimestamp("sendTime"));
                byte Status= (byte) resultSet.getInt("Status");
                String Content=null;
                Content=resultSet.getString("Content");
                byte[] Img=null;
                InputStream inputStream=null;
                if(Content==null){
                    inputStream=resultSet.getBinaryStream("Img");
                    Img=new byte[inputStream.available()];
                    inputStream.read(Img,0,inputStream.available());
                }
                byte ifSuccess= (byte) resultSet.getInt("ifSuccess");
                if(Content!=null) {
                    GroupMessage groupMessage = new GroupMessage(senderID, senderName, sendTime, Status, Content, ifSuccess);
                    groupMessages.add(groupMessage);
                }else if(inputStream!=null){
                    GroupMessage groupMessage = new GroupMessage(senderID, senderName, sendTime, Status, Img, ifSuccess);
                    groupMessages.add(groupMessage);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getChat() !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.getChat(1) when inputStream.available() !");
        } finally {
            Conn.Close();
        }
        return groupMessages;
    }

    public static ArrayList<GroupMessage> getChat(String userID, String groupID, String referredTime) {
        ArrayList<GroupMessage> groupMessages=new ArrayList<GroupMessage>();
        try {
            Timestamp timestamp= Timestamp.valueOf(referredTime);
            Connection connection=Conn.getConnection();
            String sql="SELECT * FROM group_"+groupID+"_chatdata WHERE sendTime>? AND senderID NOT IN ("+userID+")";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1,timestamp);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String senderID=resultSet.getString("senderID");
                String senderName=resultSet.getString("senderName");
                String sendTime= String.valueOf(resultSet.getTimestamp("sendTime"));
                byte Status= (byte) resultSet.getInt("Status");
                String Content=null;
                Content = resultSet.getString("Content");
                byte[] Img=null;
                InputStream inputStream=null;
                if(Content==null){
                    inputStream=resultSet.getBinaryStream("Img");
                    Img=new byte[inputStream.available()];
                    inputStream.read(Img,0,inputStream.available());
                }
                byte ifSuccess= (byte) resultSet.getInt("ifSuccess");
                if(Content!=null) {
                    GroupMessage groupMessage = new GroupMessage(senderID, senderName, sendTime, Status, Content, ifSuccess);
                    groupMessages.add(groupMessage);
                }else if(inputStream!=null){
                    GroupMessage groupMessage = new GroupMessage(senderID, senderName, sendTime, Status, Img, ifSuccess);
                    groupMessages.add(groupMessage);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.getChat() !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.getChat(2) when inputStream.available() !");
        } finally {
            Conn.Close();
        }
        return groupMessages;
    }

    public static void sendGroupChat(String groupID,String senderID, String senderName, String sendTime, String status, String content) {
        try {
            Connection connection=Conn.getConnection();
            String sql="INSERT INTO group_"+groupID+"_chatdata(senderID,senderName,sendTime,Status,Content) VALUES (?,?,?,?,?)";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,senderID);
            preparedStatement.setString(2,senderName);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(sendTime));
            preparedStatement.setInt(4, Integer.parseInt(status));
            preparedStatement.setString(5,content);
            int result=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.sendGroupChat() !");
        } finally {
            Conn.Close();
        }
    }

    public static void updateGroupInfo(String userID, model.group.Group group) {
        try {
            Connection connection=Conn.getConnection();
            String sql="UPDATE user_"+userID+"_contactlist SET nickName=?,headIcon=?,isupdate=1 WHERE ID=? AND types=1";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,group.getGroupName());
            InputStream inputStream=null;
            inputStream=new ByteArrayInputStream(group.getGroupIcon());
            preparedStatement.setBinaryStream(2,inputStream);
            preparedStatement.setString(3,group.getGroupID());
            int commitResult=preparedStatement.executeUpdate();

            if (inputStream!=null)
                inputStream.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.updateGroupInfo() !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.updateGroupInfo() when inputStream.close() !");
        } finally {
            Conn.Close();
        }
    }

    public static void updateGroupInfo(String userID,String groupID) {
        try {
            Connection connection=Conn.getConnection();
            String sql="UPDATE user_"+userID+"_contactlist SET isupdate=1 WHERE ID=? AND types=1";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,groupID);
            int commitResult=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.updateGroupInfo() !");
        } finally {
            Conn.Close();
        }
    }


    public static Map<String, SimpleGroup> checkUpdatedGroup(String userID) {
        Map<String,SimpleGroup> simpleGroupMap=new HashMap<String, SimpleGroup>();
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT * FROM user_"+userID+"_contactlist WHERE isupdate=1 AND types=1";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            ResultSet resultSet=preparedStatement.executeQuery();
            String ID;
            String nickName;
            byte[] headIcon=null;
            InputStream inputStream=null;
            while (resultSet.next()){
                headIcon=null;
                inputStream=null;
                ID=resultSet.getString("ID");
                nickName=resultSet.getString("nickName");
                inputStream=resultSet.getBinaryStream("headIcon");
                if (inputStream!=null){
                    headIcon=new byte[inputStream.available()];
                    inputStream.read(headIcon,0,inputStream.available());
                }
                SimpleGroup simpleGroup=new SimpleGroup(ID,nickName,headIcon);
                simpleGroupMap.put(ID,simpleGroup);
            }
            if(inputStream!=null)
                inputStream.close();
            sql="UPDATE user_"+userID+"_contactlist SET isupdate=0 WHERE isupdate=1 AND types=1";
            preparedStatement=connection.prepareStatement(sql);
            int result=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.checkUpdateGroup() !");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in Group.checkUpdateGroup when inputStream.available() !");
        } finally {
            Conn.Close();
        }
        return simpleGroupMap;
    }


    //更新群信息
    public static void changeGroupInfo(model.group.Group group) throws IOException {
        InputStream inputStream=null;
        inputStream=new ByteArrayInputStream(group.getGroupIcon());
        try {
            Connection connection=Conn.getConnection();
            String sql="UPDATE groups SET groupName=?,groupIntro=?,groupIcon=? WHERE groupID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,group.getGroupName());
            preparedStatement.setString(2,group.getGroupIntro());
            preparedStatement.setBinaryStream(3,inputStream);
            preparedStatement.setString(4,group.getGroupID());

            int result=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.changeGroupInfo !");
        } finally {
            if(inputStream!=null)
                inputStream.close();
            Conn.Close();
        }
    }

    public static void addGroup(String userID, model.group.Group group) {
        try {
            Connection connection=Conn.getConnection();
            String sql="INSERT INTO user_"+userID+"_contactlist(ID,nickName,headIcon,types) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,group.getGroupID());
            preparedStatement.setString(2,group.getGroupName());
            InputStream inputStream=null;
            byte[] bytes=group.getGroupIcon();
            if (bytes!=null)
                inputStream=new ByteArrayInputStream(bytes);
            preparedStatement.setBinaryStream(3,inputStream);
            preparedStatement.setInt(4,1);

            int result=preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.addGroup() !");
        } finally {
            Conn.Close();
        }
    }

    public static void quitGroup(String userID, String groupID) {
        try {
            Connection connection=Conn.getConnection();
            connection.setAutoCommit(false);
            /**将用户从群列表中删除**/
            String sql="DELETE FROM group_"+groupID+"_member WHERE userID="+userID;
            Statement statement=connection.createStatement();
            statement.addBatch(sql);
//            int result=statement.executeUpdate();

            /**将群从用户列表中删除**/
            sql="DELETE FROM user_"+userID+"_contactlist WHERE ID="+groupID;
            statement.addBatch(sql);
            int[] result=statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.quitGroup() !");
        } finally {
            Conn.Close();
        }
    }

    public static void insertImg(String groupID, String senderID, String senderName, String sendTime, byte[] img) {
        try {
            Connection connection=Conn.getConnection();
            String sql="INSERT INTO group_"+groupID+"_chatdata(senderID,senderName,sendTime,Img) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,senderID);
            preparedStatement.setString(2,senderName);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(sendTime));
            InputStream inputStream=null;
            inputStream=new ByteArrayInputStream(img);
            preparedStatement.setBinaryStream(4,inputStream);

            int result=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.insertImg() !");
        } finally {
            Conn.Close();
        }
    }

    public static void updateGroupMessage(String groupID, String content, String sendTime) {
        try {
            Connection connection=Conn.getConnection();
            String sql="UPDATE groups SET theLatestText=?,theLatestTextTime=? WHERE groupID=?";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setString(1,content);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(sendTime));
            preparedStatement.setString(3,groupID);
            int result=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Group.updateGroupMessage() !");
        } finally {
            Conn.Close();
        }
    }
}
