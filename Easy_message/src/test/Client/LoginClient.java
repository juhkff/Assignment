package test.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.NoticeMessage;
import model.User;
import tools.Online;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

public class LoginClient {
    /*private final String MESSAGE_SERVER_IP="123.207.13.112";
    private final String FILE_SERVER_IP="123.56.12.225";
    private final int MESSAGE_SERVER_PORT=1111;
    private final int FILE_SERVER_PORT=2222;
    Scanner scanner=new Scanner(System.in);*/
    private static String localIP;                                                                      ///////
    public static final String URL_ADDRESS = "http://123.207.13.112:8080/Easy_message";
    /**将线程中获得的ArrayList<NoticeMessage>存储为全局变量**/
    public static ArrayList<NoticeMessage> noticeMessages;

    public static void main(String[] args) throws Exception {

        //final String MESSAGE_SERVER_IP = "123.56.12.225";
        final String MESSAGE_SERVER_IP = "123.207.13.112";
        //final String MESSAGE_SERVER_IP = "localhost";
        final int MESSAGE_SERVER_PORT = 1111;
        SocketAddress messageSocketAddress;
        DatagramSocket messageds = null;
        byte[] messagesendby;
        byte[] messagerecby = new byte[1024 * 8];
        DatagramPacket messagedp;

        //final String FILE_SERVER_IP = "123.56.12.225";
        final String FILE_SERVER_IP = "123.207.13.112";
        //final String FILE_SERVER_IP = "localhost";
        final int FILE_SERVER_PORT = 2222;
        SocketAddress fileSocketAddress;
        DatagramSocket fileds = null;
        byte[] filesendby;
        byte[] filerecby = new byte[1024 * 8];
        DatagramPacket filedp;

        Scanner scanner = new Scanner(System.in);
        //帐号(从用户输入获得)
        /*System.out.print("请输入帐号:__________\b\b\b\b\b\b\b\b\b\b");
        String userID=scanner.next();
        System.out.print("请输入密码:");
        String passWord=scanner.next();*/

        String userID = "8076357234";
        //String userID="1005221246";
        String passWord = "aqko251068";
        String nickName;
//        /**向servlet发送请求验证帐号和密码的步骤没有写**/
        //现在写了
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userID", userID);
        parameters.put("passWord", passWord);
        Request request = new Request(URL_ADDRESS + "/Login", parameters, RequestProperty.APPLICATION);
        String result = request.doPost();     //获得验证结果
        if (result.equals("null"))
            System.out.println("帐号不存在，登录失败!");
        else if (result.equals("false"))
            System.out.println("密码错误，登录失败!");
        else if (result.equals("error"))
            System.out.println("发生了未知错误...");
        else if (result.equals("true")) {
            System.out.println("帐号密码验证成功!");
            parameters.remove("passWord");                                      //remove是否会一掉一对(还是仅仅只移除key)?
            request = new Request(URL_ADDRESS + "/GetUserInfo", parameters, RequestProperty.APPLICATION);
            String userInfo = request.doPost();                                           //userInfo:用户json数据/none
            if (userInfo.equals("none")) {
                throw new SQLException("用户数据查询出错!");
            } else {
                //userInfo即为json格式的数据
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<User>() {
                }.getType();
                User user = gson.fromJson(userInfo, type);
                System.out.println("用户的数据(已经转换为了User类对象):" + userInfo);       /**输出用户基本信息**/

                nickName = user.getNickName();                                                /**获取用户的nickName属性**/
            }


            //除非登录时向两个服务器发送的数据包都得到了应答(回复数据包)，否则重复发送
            String messageresult;
            String fileresult;


            /**创建获取好友列表的线程**/
            /**
             * 应该和后面的线程组放在一起?
             * **/
            ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList");
            Thread thread = new Thread(contactListThread);
            thread.start();

            do {
            /*messagesendby = ("Login:"+userID).getBytes();
            filesendby = ("Login:"+userID).getBytes();

            messageSocketAddress = new InetSocketAddress(MESSAGE_SERVER_IP, MESSAGE_SERVER_PORT);
            fileSocketAddress = new InetSocketAddress(FILE_SERVER_IP, FILE_SERVER_PORT);*/

                try {
                    messageds = new DatagramSocket();
                    fileds = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                //获取客户端上线时的局域网地址
                //不过这样获得的局域网地址仍然不能保证准确
                String clientIP = getLocalHostAddress().getHostAddress();
                int clientMessagePORT = messageds.getLocalPort();
                int clientFilePORT = fileds.getLocalPort();
                String clientMessageAddress = clientIP + ":" + clientMessagePORT;                 //向MessageServer发送数据包的本地局域网地址
                String clientFileAddress = clientIP + ":" + clientFilePORT;                       //向FileServer发送数据包的本地局域网地址

                System.out.println(clientMessageAddress);

                //localIP = clientMessageAddress;
                localIP = clientIP;
                messagesendby = ("Login/" + userID + "/" + clientMessageAddress).getBytes();
                filesendby = ("Login/" + userID + "/" + clientFileAddress).getBytes();

                messageSocketAddress = new InetSocketAddress(MESSAGE_SERVER_IP, MESSAGE_SERVER_PORT);
                fileSocketAddress = new InetSocketAddress(FILE_SERVER_IP, FILE_SERVER_PORT);

                messagedp = new DatagramPacket(messagesendby, 0, messagesendby.length, messageSocketAddress);
                filedp = new DatagramPacket(filesendby, 0, filesendby.length, fileSocketAddress);

                try {
                    messageds.send(messagedp);
                    messagedp.setData(messagerecby, 0, messagerecby.length);
                    messageds.receive(messagedp);

                    fileds.send(filedp);
                    filedp.setData(filerecby, 0, filerecby.length);
                    fileds.receive(filedp);
                } catch (IOException e) {
                    e.printStackTrace();
                }



            /*messagedp=new DatagramPacket(messagerecby,0,messagerecby.length,messageSocketAddress);
            filedp=new DatagramPacket(filerecby,0,filerecby.length,fileSocketAddress);*/

                messageresult = new String(messagedp.getData(), 0, messagedp.getLength());
                fileresult = new String(filedp.getData(), 0, messagedp.getLength());

            } while (!messageresult.equals("success") && !fileresult.equals("success"));

        /*Map<String, String> contactList = new HashMap<String, String>();
        contactList = getContactList();*/

        /*//创建获取好友列表的线程
        LoginThread loginThread = new LoginThread(userID,"123.207.13.112:8080/Easy_message/Login");
        Thread thread=new Thread(loginThread);
        thread.start();*/

        /*//建立接收消息的线程
        ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(messageds, messageSocketAddress);
        Thread thread1 = new Thread(receiveMessageThread);
        thread1.start();
        //建立发送心跳的线程
        HeartThread heartThread = new HeartThread(userID, messageds, fileds, messageSocketAddress, fileSocketAddress);
        Thread thread2 = new Thread(heartThread);
        thread2.start();
        //建立监听本机局域网地址的线程
        LocalAddressThread localAddressThread = new LocalAddressThread();
        Thread thread3 = new Thread(localAddressThread);
        thread3.start();
        //建立查询servlet获得状态有更新的好友的数据
        ListenerThread listenerThread = new ListenerThread(userID, "123.207.13.112:8080/Easy_message/Listener");
        Thread thread4 = new Thread(listenerThread);
        thread4.start();*/



        //创建所有线程
            startAllThread(userID, messageds, fileds, messageSocketAddress, fileSocketAddress);

            /**---------------------------------以下情景为模拟用户使用程序，注意聊天应自动建立线程(不建立线程则无法实现多窗口聊天)--------------------------------**/

            String nextCommand;
            while (true) {
                System.out.println("\n聊天/Chat\t添加联系人/Add\t退出程序/Exit\t处理请求/Deal\t");
                System.out.print("输入 您要进行的操作 :______\b\b\b\b\b\b");
                nextCommand = scanner.nextLine();
                if (/*scanner.next()*/nextCommand.equals("Chat")) {
                    System.out.println("--------------------------------------------------------------好友聊天界面--------------------------------------------------------------");
                    System.out.print("输入进行聊天的userID:______\b\b\b\b\b\b");
                    String ID;
                    int i = 0;
                    do {
                        if (i > 0)
                            System.out.print("\nID不对，请重新输入!:______\b\b\b\b\b\b");
                        i++;
                        ID = scanner.next();
                    } while (ID.length() != 10 && ID.length() != 6);
                    //取得userID
                } else if (/*scanner.nextLine()*/nextCommand.equals("Add")) {
                    System.out.println("--------------------------------------------------------------添加好友界面--------------------------------------------------------------");
                    /**--------------------添加好友------------------**/
                    Map<String, String> parameter = new HashMap<String, String>();
                    parameter.put("userID", userID);
                    Request request1 = new Request(URL_ADDRESS + "/AddContact", parameter, RequestProperty.APPLICATION);
                    String content = request1.doPost();
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<Map<String, String>>() {
                    }.getType();
                    //Map<userID,nickName>
                    Map<String, String> userList = gson.fromJson(content, type);
                    if (userList != null && userList.size() > 0) {
                        System.out.println();
                        System.out.println("可添加的联系人列表:");
                        for (String ID : userList.keySet()) {
                            String thenickName = userList.get(ID);
                            System.out.println("帐号:" + ID + "\t昵称:" + thenickName);
                        }
                        System.out.print("输入您要添加的userID:__________\b\b\b\b\b\b\b\b\b\b");
                        String receiverID;
                        int i = 0;
                        do {
                            if (i > 0)
                                System.out.print("\n格式不对，请重新输入!:__________\b\b\b\b\b\b\b\b\b\b");
                            i++;
                            receiverID = scanner.next();
                        } while (receiverID.length() != 10);
                        //得到userID
                        Map<String, String> parameters2 = new HashMap<String, String>();
                        parameters2.put("userID", userID);
                        parameters2.put("nickName", nickName);
                        parameters2.put("receiverID", receiverID);
                        Request request2 = new Request(URL_ADDRESS + "/SendRequest", parameters2, RequestProperty.APPLICATION);
                        String content1 = request2.doPost();                  //获得返回的提示信息
                        if (content1.equals("CG"))
                            System.out.println("请求已发出，请等待对方的回复!");
                        else if (content1.equals("CF"))
                            System.out.println("您已发送过该邀请，请不要重复发送!");
                        else
                            throw new Exception("发送邀请出错!LoginClient");
                /*if (contact != null && contact.size() > 0) {
                    System.out.println();
                    System.out.println("状态较刚才有更新的联系人:");
                    for (String userID : contact.keySet()) {
                        String status;
                        status = (contact.get(userID).equals("0")) ? "离线" : "在线";
                        System.out.println("userID:" + userID + "\t" + "状态变为:" + status);
                    }
                    System.out.println();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();*/
                    }
                } else if (/*scanner.nextLine()*/nextCommand.equals("Exit")) {
                    Map<String, String> parameter = new HashMap<String, String>();
                    parameter.put("userID", userID);
                    Request request2 = new Request(URL_ADDRESS + "/Exit", parameter, RequestProperty.APPLICATION);
                    String content = request2.doPost();                                    //content:error/exit两种可能
                    if (content.equals("exit")) {
                        System.out.println("exit...");
                        System.exit(1);
                    } else if (content.equals("error"))
                        System.out.println("退出异常!");
                } else if (nextCommand.equals("Deal")) {
                    System.out.print("您要处理第几个请求？请输入数字:");
                    int index=scanner.nextInt();
                    while (index<1||index>noticeMessages.size()){
                        System.out.print("数字越界，请重新输入!:");
                        index=scanner.nextInt();
                    }
                    NoticeMessage noticeMessage=noticeMessages.get(index);
                    System.out.print("\n确定您要处理的请求为:");
                    if(noticeMessage.getProperty()==0)
                        System.out.println("来自帐号"+noticeMessage.getAnotherID()+"的昵称为"+noticeMessage.getNickName()+"的好友邀请? (Y)");
                    String response=scanner.next();
                    if(!response.equals("Y")){
                        continue;
                    }else{
                        String response1 = null;
                        Map<String,String> parameters1=new HashMap<String, String>();
                        parameters1.put()
                        Request request1=new Request(URL_ADDRESS+"/Deal");
                        /**考虑的问题：传什么参数;在线和离线返回请求的区别**/
                        while(response1.equals("Y")||response1.equals("N")) {
                            System.out.println("\n同意还是拒绝? (Y/N)");
                            response1 = scanner.next();
                            if (response1.equals("Y")) {

                            } else if (response1.equals("N")) {

                            } else {

                            }
                        }
                    }
                } else if (nextCommand.equals(""))

                    //处理局域网通信的识别问题
                    //如果接收方的IP和本机IP前三位相同，则认为在同一个局域网内   可行度低，因为并不能确保这样就一定在一个局域网内
            /*设置一个全局环境变量Set<String userID>.当点开一个聊天窗口就开启一个线程，由本用户发送"LocalConnect:userID"到消息服务器，服务器解析后取得对应userID的公网地址，向其
            发送数据包令其向本用户发送"TryLocalConnect:userID"的数据包.本用户若接收到此数据包，则将userID添加到Set中(若key已存在，则更新value).表示可以与其进行局域网消息发送，并
            向服务器发送"ConnectSuccess:userID(me)userID(him/her)/"的数据包，服务器收到此数据包后开启一个监听局域网连接的线程(此线程可结束)，在此线程中取出并局部存储本机userID+
            局域网地址及连接到的用户的userID+其局域网地址，然后每隔一定时间再取出用户的局域网地址进行比较，若相同则跳过，若不同则查出并用while循环向向本机公网地址发送
            "ChangeLocalConnect:userID"，本机接收后向服务器发送"I_KNOW_LOCAL_CHANGE"，获取userID，移除Set中的相应userID，并再次开启发送"LocalConnect:userID"的线程.服务器获取
            "I_KNOW_LOCAL_CHANGE"则将用户表中isKnown改为true
            在打开至少一个聊天界面时，另一个监听线程应持续运行，此线程作用为每隔一段时间
            */
                    scanner.nextLine();
            }
        }
    }


    /**
     * ------------------------------------------------------主方法分割线--------------------------------------------------------
     **/

    //获得localIP
    private static String getLocalIP() {
        return localIP;
    }


    //开启所有线程的方法
    private static void startAllThread(String userID, DatagramSocket messageds, DatagramSocket fileds, SocketAddress messageSocketAddress, SocketAddress fileSocketAddress) throws SQLException {
        //建立接收消息的线程
        ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(messageds, messageSocketAddress);
        Thread thread = new Thread(receiveMessageThread);
        thread.start();
        //建立发送心跳的线程
        HeartThread heartThread = new HeartThread(userID, messageds, fileds, messageSocketAddress, fileSocketAddress);
        Thread thread1 = new Thread(heartThread);
        thread1.start();
        //建立监听本机局域网地址的线程
        LocalAddressThread localAddressThread = new LocalAddressThread(userID, "/findMyLocalIP");
        Thread thread2 = new Thread(localAddressThread);
        thread2.start();
        //建立查询servlet获得状态有更新的好友的数据
        ListenerThread listenerThread = new ListenerThread(userID, URL_ADDRESS + "/Listener");
        Thread thread3 = new Thread(listenerThread);
        thread3.start();
        //建立一次性线程接收最新消息提醒(就是用户刚上线时的消息提醒)
        //以及
        //建立一次性线程接收最新聊天消息提醒
        MessageNoticeThread messageNoticeThread = new MessageNoticeThread(userID);
        ChatNoticeThread chatNoticeThread = new ChatNoticeThread(userID);
        Thread thread4 = new Thread(messageNoticeThread);
        Thread thread5 = new Thread(chatNoticeThread);
        thread4.start();
        thread5.start();
    }

    //接收消息的线程类
    private static class ReceiveMessageThread implements Runnable {
        private DatagramSocket ds;
        private SocketAddress messageSocketAddress;
        private DatagramPacket dp;
        private byte[] by;
        private String message;

        public ReceiveMessageThread(DatagramSocket ds, SocketAddress messageSocketAddress) {
            this.ds = ds;
            this.messageSocketAddress = messageSocketAddress;
            by = new byte[1024 * 8];
        }

        @Override
        public void run() {
            /*接收消息的线程启动!*/
            System.out.println("等待接收...");
            while (true) {
                by = new byte[1024 * 8];
                dp = new DatagramPacket(by, 0, by.length);
                try {
                    ds.receive(dp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /**
                 * ------------------------------------对收到的dp包进行各种情况的讨论--------------------------------
                 * **/
                message = new String(dp.getData(), 0, dp.getLength());
                if (message.equals("PublicAddressChanged")) {

                    try {
                        LoginClient.main(null);
                        System.exit(1);                                                         //重启程序?
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*//发送心跳包的线程类
    private static class HeartThread implements Runnable {
        private String userID;
        private byte[] by;                      //by数组毫无用处，仅仅是为了初始化DatagramPacket对象而存在

        private DatagramSocket messageds;
        private DatagramPacket messagedp;
        private SocketAddress messageSocketAddress;
        private byte[] messageheartby;
        String M_message;

        private DatagramSocket fileds;
        private DatagramPacket filedp;
        private SocketAddress fileSocketAddress;
        private byte[] fileheartby;
        String F_message;

        public HeartThread(String userID, DatagramSocket messageds, DatagramSocket fileds, SocketAddress messageSocketAddress, SocketAddress fileSocketAddress) {
            this.userID = userID;
            this.by = "Heartbeat".getBytes();
            this.messageds = messageds;
            this.messageSocketAddress = messageSocketAddress;
            this.fileds = fileds;
            this.fileSocketAddress = fileSocketAddress;
            this.messagedp = new DatagramPacket(by, 0, by.length, messageSocketAddress);
            this.filedp = new DatagramPacket(by, 0, by.length, fileSocketAddress);
        }

        @Override
        public void run() {
            while (true) {
                //获取客户端上线时的局域网地址
                //不过这样获得的局域网地址仍然不能保证准确
                String clientIP = null;
                try {
                    clientIP = getLocalHostAddress().getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                //如果本地局域网IP发生了变化(即网络发生了变化)
                if (!localIP.equals(clientIP)) {
                    localIP = clientIP;                                                                 //将变化后的IP重新赋给localIP全局变量
                    int clientMessagePORT = messageds.getLocalPort();
                    int clientFilePORT = fileds.getLocalPort();
                    String clientMessageAddress = clientIP + ":" + clientMessagePORT;                 //向MessageServer发送数据包的本地局域网地址
                    String clientFileAddress = clientIP + ":" + clientFilePORT;                       //向FileServer发送数据包的本地局域网地址
                    this.messageheartby = ("Heartbeat/" + userID + "/" + clientMessageAddress).getBytes();
                    this.fileheartby = ("Heartbeat/" + userID + "/" + clientFileAddress).getBytes();
                } else {
                    this.messageheartby = ("Heartbeat/" + userID + "/" + "NO").getBytes();
                    this.fileheartby = ("Heartbeat/" + userID + "/" + "NO").getBytes();
                }
                messagedp.setData(messageheartby, 0, messageheartby.length);
                filedp.setData(fileheartby, 0, fileheartby.length);
                try {
                    *//*messagedp = new DatagramPacket(messageheartby, 0, messageheartby.length, messageSocketAddress);
                    filedp = new DatagramPacket(fileheartby, 0, fileheartby.length, fileSocketAddress);*//*

                    //这里没办法保证收发的成功率，但暂时也只能听天由命了
                    messageds.send(messagedp);
                    fileds.send(filedp);
                } catch (IOException e) {
                    System.out.println("\n心跳发送失败!\n");
                    e.printStackTrace();
                }
                try {
                    *//*Thread.sleep(100000);*//*
                    //减小睡眠时间来保证一下udp连接
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.out.println("\n心跳线程睡眠发生异常!\n");
                    e.printStackTrace();
                    break;                                              //发生异常则跳出循环，这意味着程序失效(缺少了心跳包建立与服务器的联系)
                }
            }
        }
    }*/


    //发送心跳包
    private static class HeartThread implements Runnable {
        private String userID;
        private byte[] by;                      //by数组毫无用处，仅仅是为了初始化DatagramPacket对象而存在

        private DatagramSocket messageds;
        private DatagramPacket messagedp;
        private SocketAddress messageSocketAddress;

        private DatagramSocket fileds;
        private DatagramPacket filedp;
        private SocketAddress fileSocketAddress;

        public HeartThread(String userID, DatagramSocket messageds, DatagramSocket fileds, SocketAddress messageSocketAddress, SocketAddress fileSocketAddress) {
            this.userID = userID;
            this.by = ("Heartbeat:" + this.userID).getBytes();
            this.messageds = messageds;
            this.messageSocketAddress = messageSocketAddress;
            this.fileds = fileds;
            this.fileSocketAddress = fileSocketAddress;
            this.messagedp = new DatagramPacket(by, 0, by.length, this.messageSocketAddress);
            this.filedp = new DatagramPacket(by, 0, by.length, this.fileSocketAddress);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    messageds.send(messagedp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fileds.send(filedp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //监听本机局域网地址的线程
    private static class LocalAddressThread implements Runnable {
        private String currentIP = localIP;
        private String searchIP;
        private String userID;
        private String urlAddress;                                                                              //监听的地址(最后一部分)
        private Map<String, String> requestParameters = new HashMap<String, String>();

        private LocalAddressThread(String userID, String urlAddress) throws SQLException {
            this.userID = userID;
            this.urlAddress = urlAddress;
            requestParameters.put("userID", this.userID);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    searchIP = finaMyCurrentLocalIP();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (currentIP.equals(searchIP)) {
                    try {
                        /**System.out.println(currentIP + "   " + searchIP + "   " + "客户端地址正常");**/
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    /**
                     * 如何结束main()方法?
                     * **/
                    System.out.println(currentIP + "  " + searchIP + "我想结束main方法");

                    /**
                     * 如何重启main()方法?
                     * **/
                    try {
                        LoginClient.main(null);
                        System.exit(1);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /* private String findMyLocalIP() {
             String mylocalAddress;
             Request request = new Request(URL_ADDRESS + urlAddress, requestParameters, RequestProperty.APPLICATION);
             mylocalAddress = request.doPost();
             return mylocalAddress;
         }*/
        private String finaMyCurrentLocalIP() throws UnknownHostException {
            return getLocalHostAddress().getHostAddress();
        }
    }


    //用户登录时取得自己的局域网地址(包括IP和端口)
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


    //监听好友中的上下线行为(通过定时访问数据库实现)
    private static class ListenerThread implements Runnable {
        private String userID;
        private String urlAddress;
        private HttpURLConnection uRLConnection;
        private URL url;
        private String resultInfo;

        private ListenerThread(String userID, String urlAddress) throws SQLException {
            this.userID = userID;
            this.urlAddress = urlAddress;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5000);                    //睡眠5秒钟
                } catch (InterruptedException e) {
                    System.out.println("睡眠失败!/ListenerThread监听好友上下线行为");
                    e.printStackTrace();
                }
                try {
                    url = new URL(urlAddress);
                    uRLConnection = (HttpURLConnection) url.openConnection();
                    uRLConnection.setDoInput(true);
                    uRLConnection.setDoOutput(true);
                    uRLConnection.setRequestMethod("POST");
                    uRLConnection.setUseCaches(false);
                    uRLConnection.setInstanceFollowRedirects(true);
                    uRLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    uRLConnection.connect();

                    DataOutputStream dataOutputStream = new DataOutputStream(uRLConnection.getOutputStream());
                    String content = "userID=" + URLEncoder.encode(userID, "UTF-8");                             //发送userID,servlet用getParameter()接收
                    dataOutputStream.writeBytes(content);
                    dataOutputStream.flush();
                    dataOutputStream.close();

                    InputStream inputStream = uRLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String response = "";
                    String readLine = null;
                    while ((readLine = bufferedReader.readLine()) != null) {/**  readLine!=""中readLine是不是指被等号赋值后的readLine?  **/
                        //response=bufferedReader.readLine();
                        response = response + readLine;
                    }
                    inputStream.close();
                    bufferedReader.close();
                    uRLConnection.disconnect();
                    resultInfo = response;                                                                            //response或者resultInfo即为从servlet获得的结果
                    //从resultInfo中获得每个单一的对象(resultInfo是json格式的数据)
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<Map<String, String>>() {
                    }.getType();
                    Map<String, String> contact = gson.fromJson(resultInfo, type);

                    /***
                     * contact即是每个联系人的Map对象，键值对为<userID,status>格式.
                     * 具体的应用就看客户端了
                     * 以下为模拟应用
                     * */
                    if (contact != null && contact.size() > 0) {
                        System.out.println();
                        System.out.println("状态较刚才有更新的联系人:");
                        for (String userID : contact.keySet()) {
                            String status;
                            status = (contact.get(userID).equals("0")) ? "离线" : "在线";
                            System.out.println("userID:" + userID + "\t" + "状态变为:" + status);
                        }
                        System.out.println();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //获取联系人列表的线程
    private static class ContactListThread implements Runnable {
        private String userID;
        private String urlAddress;
        private HttpURLConnection uRLConnection;
        private URL url;
        private String resultInfo;

        public ContactListThread(String userID, String urlAddress) {
            this.userID = userID;
            this.urlAddress = urlAddress;
        }

        @Override
        public void run() {
            try {
                url = new URL(urlAddress);
                uRLConnection = (HttpURLConnection) url.openConnection();
                uRLConnection.setDoInput(true);
                uRLConnection.setDoOutput(true);
                uRLConnection.setRequestMethod("POST");
                uRLConnection.setUseCaches(false);
                uRLConnection.setInstanceFollowRedirects(true);
                uRLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                uRLConnection.connect();

                DataOutputStream dataOutputStream = new DataOutputStream(uRLConnection.getOutputStream());
                String content = "userID=" + URLEncoder.encode(userID, "UTF-8");                             //发送userID,servlet用getParameter()接收
                dataOutputStream.writeBytes(content);
                dataOutputStream.flush();
                dataOutputStream.close();

                InputStream inputStream = uRLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String response = "";
                String readLine = null;
                while ((readLine = bufferedReader.readLine()) != null) {                        /**  readLine!=""中readLine是不是指被等号赋值后的readLine?  **/
                    //response=bufferedReader.readLine();
                    response = response + readLine;
                }
                inputStream.close();
                bufferedReader.close();
                uRLConnection.disconnect();
                resultInfo = response;                                                                            //response或者resultInfo即为从servlet获得的结果
                //从resultInfo中获得每个单一的对象(resultInfo是json格式的数据)
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> contact = gson.fromJson(resultInfo, type);

                /***
                 * contact即是每个联系人的Map对象，键值对为<userID,status>格式.
                 * 具体的应用就看客户端了
                 * 以下为模拟应用
                 * */

                if (contact != null && contact.size() > 0) {
                    System.out.println();
                    System.out.println("您的联系人列表:");
                    for (String userID : contact.keySet()) {
                        String status;
                        status = (contact.get(userID).equals("0")) ? "离线" : "在线";
                        System.out.println("userID:" + userID + "\t" + "状态:" + status);
                    }
                    System.out.println();
                } else {
                    System.out.println("暂无联系人!");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final static class MessageNoticeThread implements Runnable {
        private String userID;
        private Map<String, String> parameter = new HashMap<String, String>();
        private Request request;
        private String notices;                                                 //收到的消息提醒

        public MessageNoticeThread(String userID) {
            this.userID = userID;
            this.parameter.put("userID", this.userID);
            this.request = new Request(URL_ADDRESS + "/GetNotice", this.parameter, RequestProperty.APPLICATION);
        }

        @Override
        public void run() {
            this.notices = this.request.doPost();                             //获得收到的消息提醒
            if (notices.equals("") || notices.equals("none"))
                System.out.println("您离线时未收到过任何(如添加好友)请求!");
            else {
                /**解析Json**/
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<ArrayList<NoticeMessage>>() {
                }.getType();
                noticeMessages= gson.fromJson(this.notices, type);
                for (NoticeMessage noticeMessage : noticeMessages) {
                    String anotherID = noticeMessage.getAnotherID();
                    String nickName = noticeMessage.getNickName();
                    byte property = noticeMessage.getProperty();
                    if (property == 0)                                             //好友邀请
                        System.out.println("来自帐号:" + anotherID + "  昵称为" + nickName + " 的用户的好友邀请");
                    else if (property == 1)
                        System.out.println("1");
                    else if (property == 2)
                        System.out.println("2");
                    else if (property == 3)
                        System.out.println("3");
                }
            }
            /**
             * System.out.println("--------------------------------------------------------------消息提示界面--------------------------------------------------------------");
             * String content=request.doPost();
             **/
        }
    }

    private final static class ChatNoticeThread implements Runnable {
        private String userID;
        private Map<String, String> parameter = new HashMap<String, String>();
        private Request request;
        private String notices;

        public ChatNoticeThread(String userID) {
            this.userID = userID;
            this.parameter.put("userID", this.userID);
            this.request = new Request(URL_ADDRESS + "/GetChat", this.parameter, RequestProperty.APPLICATION);
        }

        @Override
        public void run() {
            this.notices = this.request.doPost();
            if (notices.equals("") || notices.equals("none"))
                System.out.println("您离线时未收到过任何消息!");
            else {
                /**解析Json**/
            }
        }
    }


    /*// 正确的IP拿法，即优先拿site-local地址
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

}