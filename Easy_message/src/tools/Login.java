package tools;


import connection.Conn;

import model.message.ChatMessage;
import model.contact.Contact;
import model.message.ContactMessage;
import model.message.NoticeMessage;
import model.property.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

//实现登录功能
public class Login {

    //帐号和密码验证
    public static String pswVerification(String userID) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "SELECT passWord FROM userInfo WHERE userID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userID);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            return resultSet.getString("passWord");
        }
        Conn.Close();
        return null;
    }

    //用户登录时更改好友表中自己的状态
    public static int changeStatus(String userID) throws SQLException {
        Connection connection = Conn.getConnection();
        connection.setAutoCommit(false);
        String sql = "SELECT ID FROM user_" + userID + "_contactlist WHERE types=0";            //查询自己好友列表，得到所有好友ID的集合
        int[] i = new int[0];
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String sql1;
            Statement statement1 = connection.createStatement();
            //Set<String> IDList=new HashSet<String>();
            while (resultSet.next()) {
                //IDList.add(resultSet.getString("ID"));
                sql1 = "UPDATE user_" + resultSet.getString("ID") + "_contactlist SET status=1,isupdate=1 WHERE ID=" + userID;

                statement1.addBatch(sql1);
            }
            sql1 = "UPDATE userinfo SET isOnline=1 WHERE userID=" + userID;
            statement1.addBatch(sql1);
            i = statement1.executeBatch();
            connection.commit();
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
        return result;
    }

    //用户登录时更改群表中自己的状态
    public final static void changeStatusInGroup(String userID) {
        try {
            Connection connection = Conn.getConnection();
            connection.setAutoCommit(false);
            String sql = "SELECT ID FROM user_" + userID + "_contactlist WHERE types=1";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String sql1;
            while (resultSet.next()) {
                sql1 = "UPDATE group_" + resultSet.getString("ID") + "_member SET userOnline=1,isUpdate=1 WHERE userID="+userID;
                statement.addBatch(sql1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in Login.changeStatusInGroup!");
        } finally {
            Conn.Close();
        }
    }


    //用户登录时获得所有好友的状态(在线or离线)
    public static ArrayList<Contact> getContactList(String userID) throws SQLException {
        ArrayList<Contact> contactList = new ArrayList<Contact>();
        String ID;
        String nickName;
        InputStream inputStream;
        byte[] headIcon;
        byte types;
        boolean status;
        try {
            Connection connection = Conn.getConnection();
            Statement statement = connection.createStatement();
            String sql = "UPDATE user_" + userID + "_contactlist SET isupdate=0 WHERE isupdate=1";
            statement.execute(sql);
            sql = "SELECT ID,nickName,headIcon,types,status FROM user_" + userID + "_contactlist WHERE types=0";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                ID = resultSet.getString("ID");
                nickName = resultSet.getString("nickName");
                inputStream = resultSet.getBinaryStream("headIcon");
                headIcon = null;
                if (inputStream != null) {
                    headIcon = new byte[inputStream.available()];
                    inputStream.read(headIcon, 0, inputStream.available());
                    inputStream.close();
                }
                types = resultSet.getByte("types");
                status = resultSet.getBoolean("status");
                Contact contact = new Contact(ID, nickName, headIcon, types, status);
                contactList.add(contact);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }
        return contactList;
    }

    public static final Timestamp getExitTime(String userID) throws SQLException {
        Timestamp timestamp = null;
        try {
            Connection connection = Conn.getConnection();
            String sql = "SELECT exitTime FROM userinfo WHERE userID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                i++;
                timestamp = resultSet.getTimestamp("exitTime");
            }
            if (i > 1) {
                throw new SQLException("数据重复!Login.getExitTime");
            }
        } finally {
            Conn.Close();
        }
        return timestamp;
    }

    public static final ArrayList<ChatMessage> getChatMessage(String userID, Timestamp timestamp) throws SQLException, IOException {
        ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

        String anotherID;
        byte nature;
        String sendTime;
        String message = null;
        byte[] imgBytes = null;
        InputStream inputStream = null;
        ChatMessage chatMessage;

        Connection connection = Conn.getConnection();
        String sql = "SELECT * FROM user_" + userID + "_chatdata AS A WHERE sendTime=(SELECT MAX(sendTime) FROM user_" + userID + "_chatdata WHERE anotherID=A.anotherID ) AND nature IN (1,6) AND sendTime > \'" + timestamp + "\'";        /**可能会出错**/
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            anotherID = resultSet.getString("anotherID");
            nature = resultSet.getByte("nature");
            sendTime = String.valueOf(resultSet.getTimestamp("sendTime"));                                   /**********能否强制转换?**********/
            message = resultSet.getString("message");
            /**
             * if (message!=null)
             message = Chat.decodeChinese(resultSet.getString("message"));
             **/
            //if(message==null){
            /**---发送的可能情况说明了不可能出现文本和图片都为null值的情况---**/
            //imgBytes
            //}
            inputStream = resultSet.getBinaryStream("img");
            if (inputStream != null) {
                imgBytes = new byte[inputStream.available()];
                inputStream.read(imgBytes, 0, inputStream.available());
            }

            if (message != null) {
                chatMessage = new ChatMessage(anotherID, nature, sendTime, message);
                chatMessages.add(chatMessage);
            } else if (inputStream != null) {
                chatMessage = new ChatMessage(anotherID, nature, sendTime, imgBytes);
                chatMessages.add(chatMessage);
            }
        }
        Conn.Close();
        return chatMessages;
    }

    public static ArrayList<NoticeMessage> getNoticeMessage(String userID, Timestamp timestamp) throws SQLException {
        ArrayList<NoticeMessage> noticeMessages = new ArrayList<NoticeMessage>();
        NoticeMessage noticeMessage;
        Connection connection = Conn.getConnection();
        String sql = "SELECT * FROM user_" + userID + "_noticelist";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery(sql);
        while (resultSet.next()) {
            String anotherID = resultSet.getString("anotherID");
            String nickName = resultSet.getString("nickName");
            byte property = resultSet.getByte("property");
            noticeMessage = new NoticeMessage(anotherID, nickName, property);
            noticeMessages.add(noticeMessage);
        }
        Conn.Close();
        return noticeMessages;
    }

    public final static User getUserInfo(String userID) throws Exception {
        Connection connection = Conn.getConnection();
        String sql = "SELECT * FROM userinfo WHERE userID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, userID);
        ResultSet resultSet = preparedStatement.executeQuery();
        User user = null;
        int i = 0;
        while (resultSet.next()) {
            String nickName = resultSet.getString("nickName");
            InputStream inputStream = resultSet.getBinaryStream("headIcon");
            byte[] headIcon = null;
            if (inputStream != null) {
                headIcon = new byte[inputStream.available()];
                inputStream.read(headIcon, 0, inputStream.available());
                inputStream.close();
            }
            boolean isMale = resultSet.getBoolean("isMale");                          /**似乎默认为false**/
            String email = resultSet.getString("email");                              /**数据为空时无此条数据**/
            String phoneNum = resultSet.getString("phoneNum");
            String exitTime = String.valueOf(resultSet.getTimestamp("exitTime"));
            String birthday = String.valueOf(resultSet.getTimestamp("birthday"));    /**若无则json中表示为字符串null**/
            user = new User(userID, nickName, headIcon, isMale, email, phoneNum, exitTime, birthday);
            i++;
        }
        Conn.Close();
        if (i == 1 && user != null) {
            return user;
        } else {
            throw new Exception("查询用户不只一个!tools.Login.getUserInfo");
        }
    }

    public final static void deleteAllNoticeMessage(String userID) throws SQLException {
        Connection connection = Conn.getConnection();
        String sql = "DELETE FROM user_" + userID + "_noticelist";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();

        Conn.Close();
    }


    public static ArrayList<ContactMessage> getContactMessageList(String userID, String exitTime) {
        ArrayList<ContactMessage> contactMessages=new ArrayList<ContactMessage>();
        try {
            Connection connection=Conn.getConnection();
            String sql="SELECT count(1),anotherID FROM user_"+userID+"_chatdata WHERE sendTime>? GROUP BY anotherID";
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(exitTime));

            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()) {
                String anotherID=resultSet.getString("anotherID");
                User user=Online.findUserByUserID(anotherID);
                String nickName=user.getNickName();
                byte[] headIcon=user.getHeadIcon();
                int messageNum=resultSet.getInt("count(1)");
                ContactMessage contactMessage=new ContactMessage(anotherID,nickName,headIcon,messageNum);
                contactMessages.add(contactMessage);
            }

            sql="SELECT anotherID,message FROM user_"+userID+"_chatdata WHERE sendTime IN (SELECT max(sendTime) FROM user_"+userID+"_chatdata group by anotherID)  group by anotherID";
            preparedStatement=connection.prepareStatement(sql);
            resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String anotherID=resultSet.getString("anotherID");
                String message=resultSet.getString("message");
                for(ContactMessage contactMessage:contactMessages){
                    if(contactMessage.getUserID().equals(anotherID)){
                        contactMessage.setTheLattestmessage(message);
                    }
                }
            }

            sql="SELECT ID FROM user_"+userID+"_contactlist WHERE types=1";
            preparedStatement=connection.prepareStatement(sql);
            resultSet=preparedStatement.executeQuery();
            ArrayList<String> IDList=new ArrayList<String>();
            while (resultSet.next()){
                String ID=resultSet.getString("ID");
                IDList.add(ID);
            }
            ArrayList<ContactMessage> groupContact=new ArrayList<ContactMessage>();
            //获得每个群里在某一时间点之后的信息:数量，最新消息
            for(String ID:IDList){
                sql="SELECT * FROM groups WHERE groupID="+ID;
                preparedStatement=connection.prepareStatement(sql);
//                preparedStatement.setString(1,exitTime);
                resultSet=preparedStatement.executeQuery();
//                int messageNum=0;
//                String message;
                while (resultSet.next()){
//                    messageNum++;
                    String groupID=resultSet.getString("groupID");
                    String groupName=resultSet.getString("groupName");
                    InputStream inputStream=null;
                    byte[] headIcon=null;
                    inputStream=resultSet.getBinaryStream("groupIcon");
                    if (inputStream!=null){
                        headIcon=new byte[inputStream.available()];
                        inputStream.read(headIcon,0,inputStream.available());
                        inputStream.close();
                    }
                    String theLattestMessage=resultSet.getString("theLatestText");
                    ContactMessage contactMessage=new ContactMessage(ID,groupName,headIcon,theLattestMessage);
                    groupContact.add(contactMessage);
                }
            }
            int i=0;
            for(String ID:IDList){
                sql="SELECT * FROM group_"+ID+"_chatdata WHERE sendTime>?";
                preparedStatement=connection.prepareStatement(sql);
                preparedStatement.setTimestamp(1, Timestamp.valueOf(exitTime));
                resultSet=preparedStatement.executeQuery();
                int messageNum=0;
                while (resultSet.next()){
                    messageNum++;
                }
                groupContact.get(i).setMessageNum(messageNum);
                contactMessages.add(groupContact.get(i));                                       //将群对象也添加到这个列表中
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Conn.Close();
        }

        return contactMessages;
    }
}








/*    //用户登录时取得自己的局域网地址(包括IP和端口)
    //这个方法是从网络上搬运的
    public static InetAddress getLocalHostAddress() throws UnknownHostException {
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();

                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        if (jdkSuppliedAddress == null) {
            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
        }
        return jdkSuppliedAddress;
    }
    */


    /* 正确的IP拿法，即优先拿site-local地址
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }*/
