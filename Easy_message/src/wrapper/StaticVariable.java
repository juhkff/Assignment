package wrapper;

import model.contact.Contact;
import model.group.SimpleGroup;
import model.message.ChatMessage;
import model.message.NoticeMessage;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaticVariable {
    /**
     * 设置全局变量
     **/
    //和每个用户的聊天记录
    public static Map<String,ArrayList<ChatMessage>> chatMessagesList=new HashMap<String, ArrayList<ChatMessage>>();

    public static final String URL_ADDRESS = "http://123.207.13.112:8080/Easy_message";

    //将线程中获得的ArrayList<NoticeMessage>存储为全局变量
//    public static ArrayList<NoticeMessage> noticeMessages/*=new ArrayList<NoticeMessage>()*/;
    /**
     * 好友列表
     **/
    public static Map<String, Contact> contacts=new HashMap<String, Contact>();
    /**
     * 群列表
     **/
    public static Map<String, SimpleGroup> groups = new HashMap<String, SimpleGroup>();

    //存储接发过程中的一些判断
    //public static ArrayList<TransmitModel> ThreadPools=new ArrayList<TransmitModel>();
    public static Map<Integer, Boolean> ifis = new HashMap<Integer, Boolean>();
    public static Map<Integer, String> senderFileAddress = new HashMap<Integer, String>();
    public static Map<Integer, String> receiverFileAddress = new HashMap<Integer, String>();
    public static int Thread_Index = 1;

    //用于退出群时终止相关监听线程的判断
    public static Map<String, Boolean> ifbreak = new HashMap<String, Boolean>();

    //用于用户退出群界面时终止相关监听线程的判断
    public static Map<String, Boolean> ifout = new HashMap<String, Boolean>();

    //接受的消息的存储地
//    public static BlockingQueue<ChatMessage> chatMessages=new ArrayBlockingQueue<ChatMessage>(1);



    /**与udp相关的基础工作**/
    //public final static String MESSAGE_SERVER_IP = "123.56.12.225";
    public final static String MESSAGE_SERVER_IP = "123.207.13.112";

    //final String MESSAGE_SERVER_IP = "localhost";
    public final static int MESSAGE_SERVER_PORT = 1111;
    public static SocketAddress messageSocketAddress = new InetSocketAddress(MESSAGE_SERVER_IP, MESSAGE_SERVER_PORT);
    public static DatagramSocket messageds;

    static {
        try {
            messageds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static byte[] messagesendby;
//    public static byte[] messagerecby = new byte[1024 * 8];
    public static DatagramPacket messagedp;

    //public static final String FILE_SERVER_IP = "123.56.12.225";
    public static final String FILE_SERVER_IP = "123.207.13.112";
    //final String FILE_SERVER_IP = "localhost";
    public static final int FILE_SERVER_PORT = 2222;
    public static final int FILE_SERVER_SOCKET_PORT=3333;
    public static final int FILE_SERVER_SOCKET_DOWNLOAD_PORT=4444;
    public static final int FILE_SERVER_SOCKET_JSON_PORT=6666;
    public static SocketAddress fileSocketAddress = new InetSocketAddress(FILE_SERVER_IP, FILE_SERVER_PORT);
    public static DatagramSocket fileds;

    static {
        try {
            fileds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /*public static byte[] filesendby;
    public static byte[] filerecby = new byte[1024 * 8];
    public static DatagramPacket filedp;*/


    public static final String USERINFO_SAVE_PATH="C:\\Easy_message\\UserInfo";
    public static final String USERINFO_FRIENDS_LIST="friends.json";
    public static final String USERINFO_GROUPS_LIST="groups.json";
    public static final String USERINFO_SORT_FRIENDS="sort_friends.json";
    public static final String USERINFO_SORT_GROUPS="sort_groups.json";
    public static String getUserInfoSavePath(String userID){
        return USERINFO_SAVE_PATH+"\\"+userID;                                      //末尾不带\\符号
    }

    public static String getUserinfoFriendsList(String userID){
        return getUserInfoSavePath(userID)+"\\"+USERINFO_FRIENDS_LIST;
    }

    public static String getUserinfoGroupsList(String userID){
        return getUserInfoSavePath(userID)+"\\"+USERINFO_GROUPS_LIST;
    }

    public static String getUserinfoSortFriends(String userID){
        return getUserInfoSavePath(userID)+"\\"+USERINFO_SORT_FRIENDS;
    }

    public static String getUserinfoSortGroups(String userID){
        return getUserInfoSavePath(userID)+"\\"+USERINFO_SORT_GROUPS;
    }

    public static String overMark="over";
    public static String continueMark="continue";

    public static boolean isOver(){
        return false;
    }

}
