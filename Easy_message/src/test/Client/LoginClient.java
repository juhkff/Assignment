package test.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.ChatMessage;
import model.Contact;
import model.NoticeMessage;
import model.User;
import tools.Chat;
import tools.DateTime;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class LoginClient {
    /*private static String localIP;     */                                                                 ///////
    public static final String URL_ADDRESS = "http://123.207.13.112:8080/Easy_message";
    /**
     * 将线程中获得的ArrayList<NoticeMessage>存储为全局变量
     **/
    public static ArrayList<NoticeMessage> noticeMessages;
    public static Map<String, Contact> contacts;

    /**
     * 小细节：这里要是不用Map<String,Contact>类型而用<ArrayList>类型的话，在根据ID查找用户上就会很吃效率(大概)
     **/

    //public static DatagramSocket messageds = null;
    //public static DatagramSocket fileds = null;
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
        //String userID="5329421786";
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


            do {
                try {
                    messageds = new DatagramSocket();
                    fileds = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                //获取客户端上线时的局域网地址
                /**暴力获取**/
                int clientMessagePORT = messageds.getLocalPort();
                int clientFilePORT = fileds.getLocalPort();
                ArrayList<String> messageAddress = getLocalMessageAddress(clientMessagePORT);
                ArrayList<String> fileAddress = getLocalFileAddress(clientFilePORT);

                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                String messageAddressList = gson.toJson(messageAddress);
                String fileAddressList = gson.toJson(fileAddress);


                /*String clientMessageAddress = clientIP + ":" + clientMessagePORT;                 //向MessageServer发送数据包的本地局域网地址
                String clientFileAddress = clientIP + ":" + clientFilePORT;                       //向FileServer发送数据包的本地局域网地址
                System.out.println(clientMessageAddress);*/

                //localIP = clientMessageAddress;
                messagesendby = ("Login/" + userID + "/" + messageAddressList).getBytes();
                filesendby = ("Login/" + userID + "/" + fileAddressList).getBytes();

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

            //建立接收消息的线程
            ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(messageds, messageSocketAddress);
            Thread thread = new Thread(receiveMessageThread);
            thread.start();

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
                    System.out.print("\n输入进行聊天的userID(0退出):______\b\b\b\b\b\b");
                    String anotherID;
                    int i = 0;
                    if (scanner.next().equals("0"))
                        continue;
                    do {
                        if (i > 0)
                            System.out.print("\nID不对，请重新输入!:______\b\b\b\b\b\b");
                        i++;
                        anotherID = scanner.next();
                    } while (anotherID.length() != 10 && anotherID.length() != 6);
                    //取得userID
                    /**
                     * 打开聊天界面
                     * **/
                    Map<String, String> parameters1 = new HashMap<String, String>();
                    parameters1.put("userID", userID);
                    parameters1.put("anotherID", anotherID);
                    Request request1 = new Request(URL_ADDRESS + "/getChatHistory", parameters1, RequestProperty.APPLICATION);
                    String chatHistoryList = request1.doPost();
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ArrayList<ChatMessage>>() {
                    }.getType();
                    ArrayList<ChatMessage> historyChatMessages = gson.fromJson(chatHistoryList, type);                  //historyChatMessages存储聊天记录
                    int size = historyChatMessages.size();
                    if (size > 7)
                        size = 5;                                                                                        //历史记录过多（大于7）则只输出后5条
                    for (int index = historyChatMessages.size() - size; index < historyChatMessages.size(); index++) {
                        ChatMessage chatMessage = historyChatMessages.get(index);
                        if (chatMessage.getNature() == 1)                   //1:他向我说; 0:我向他说
                            System.out.println("" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                        else
                            System.out.println("\t" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                    }
                    DateTime dateTime = new DateTime();
                    byte nature = 0;
                    while (true) {
                        System.out.println("\n发送消息(Exit退出):");
                        scanner.nextLine();
                        String message = scanner.nextLine();
                        if (message.equals("Exit"))
                            break;
                        else {
                            boolean isOnline = Chat.checkOnlineStatus(anotherID);
                            if (isOnline) {
                                System.out.println("在线，尝试外网发送...");
                                //若是对方在线
                                /**
                                 * 优先用外网发送
                                 * **/
                                String sendTime = String.valueOf(dateTime.getCurrentDateTime());
                                ExecutorService executorService = Executors.newCachedThreadPool();
                                /*synchronized (thread){
                                    try {
                                        thread.wait();
                                    }catch (InterruptedException e){
                                        e.printStackTrace();
                                    }
                                }*/
                                WaitThread waitThread=new WaitThread(thread);
                                Thread thread1=new Thread(waitThread);
                                thread1.start();
                                //thread.wait();
                                ChatSendTask chatSendTask = new ChatSendTask(messageds, messageSocketAddress, userID, anotherID, nature, sendTime, message);
                                Future<Boolean> future = executorService.submit(chatSendTask);
                                Boolean chatSendResult = false;
                                try {
                                    chatSendResult = future.get(1, TimeUnit.SECONDS);
                                } catch (TimeoutException e) {
                                    System.out.println("外网发送无效，尝试局域网连接...");
                                    executorService.shutdownNow();
                                    executorService.shutdown();
                                    /**尝试局域网连接**/
                                    Map<String,String> parameters2=new HashMap<String, String>();
                                    parameters2.put("senderID",userID);
                                    parameters2.put("anotherID",anotherID);
                                    Request request2=new Request(LoginClient.URL_ADDRESS+"/GetLocalAddress",parameters2,RequestProperty.APPLICATION);
                                    String addressList=request2.doPost();
                                    String addresses=addressList.split("/")[1];
                                    addressList=addressList.split("/")[0];
                                    Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
                                    Type type1=new TypeToken<ArrayList<String>>(){
                                    }.getType();
                                    ArrayList<String> addrList=new ArrayList<String>();
                                    addrList=gson1.fromJson(addresses,type1);
                                    //ArrayList<String> anotherAddrList=gson1.fromJson(addressList,type1);
                                    for(int index=0;index<addrList.size();index++){
                                        String address=addrList.get(index);
                                        ExecutorService executorService1 = Executors.newCachedThreadPool();
                                        ChatLocalSendTask chatLocalSendTask = new ChatLocalSendTask(messageds, userID, anotherID, nature, sendTime, message,address,addressList);
                                        Future<Boolean> future1 = executorService1.submit(chatLocalSendTask);
                                        Boolean chatLocalSendResult = false;
                                        try {
                                            chatLocalSendResult = future1.get(1, TimeUnit.SECONDS);
                                        } catch (TimeoutException e1){
                                            if(index<addrList.size()-1)
                                                continue;
                                            else {
                                                System.out.println("局域网发送无效，尝试数据库离线式发送...");
                                                Map<String, String> parameters3 = new HashMap<String, String>();
                                                parameters3.put("userID", userID);
                                                parameters3.put("anotherID", anotherID);
                                                parameters3.put("message", message);
                                                parameters3.put("sendTime", sendTime);
                                                Request request3 = new Request(URL_ADDRESS + "/OfflineChar", parameters3, RequestProperty.APPLICATION);
                                                String result3 = request3.doPost();               //result2 : success / false;
                                                System.out.println(result3);
                                            }
                                        }
                                    }
                                    /*ExecutorService executorService1 = Executors.newCachedThreadPool();
                                    ChatLocalSendTask chatLocalSendTask = new ChatLocalSendTask(messageds, userID, anotherID, nature, sendTime, message);
                                    Future<Boolean> future1 = executorService1.submit(chatLocalSendTask);
                                    Boolean chatLocalSendResult = false;
                                    try {
                                        chatLocalSendResult = future1.get(1, TimeUnit.SECONDS);
                                    } catch (TimeoutException e1) {
                                        System.out.println("局域网发送无效，尝试数据库离线式发送...");
                                        Map<String, String> parameters2 = new HashMap<String, String>();
                                        parameters2.put("userID", userID);
                                        parameters2.put("anotherID", anotherID);
                                        parameters2.put("message", message);
                                        parameters2.put("sendTime", sendTime);
                                        Request request2 = new Request(URL_ADDRESS + "/OfflineChar", parameters2, RequestProperty.APPLICATION);
                                        String result2 = request2.doPost();               //result2 : success / false;
                                        System.out.println(result2);
                                    }*/
                                }
                                /*synchronized (thread) {
                                    thread.notify();
                                }*/
                                NotifyThread notifyThread=new NotifyThread(thread);
                                Thread thread2=new Thread(notifyThread);
                                thread2.start();
                            }else {
                                String sendTime = String.valueOf(dateTime.getCurrentDateTime());
                                Map<String, String> parameters3 = new HashMap<String, String>();
                                parameters3.put("userID", userID);
                                parameters3.put("anotherID", anotherID);
                                parameters3.put("message", message);
                                parameters3.put("sendTime", sendTime);
                                Request request3 = new Request(URL_ADDRESS + "/OfflineChar", parameters3, RequestProperty.APPLICATION);
                                String result3 = request3.doPost();               //result2 : success / false;
                                System.out.println(result3);
                            }
                        }
                    }
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
                        System.out.print("输入您要添加的userID(0退出):__________\b\b\b\b\b\b\b\b\b\b");
                        String receiverID;
                        int i = 0;
                        if (scanner.next().equals("0"))
                            continue;
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
                    System.out.print("您要处理第几个请求？请输入数字(0为退出):");
                    int index = scanner.nextInt();
                    if (index == 0) {
                        continue;
                    }
                    while (index < 1 || index > noticeMessages.size()) {
                        System.out.print("数字越界，请重新输入!:");
                        index = scanner.nextInt();
                    }
                    NoticeMessage noticeMessage = noticeMessages.get(index - 1);
                    System.out.print("\n确定您要处理的请求为:");
                    if (noticeMessage.getProperty() == 0)
                        System.out.println("来自帐号" + noticeMessage.getAnotherID() + "的昵称为" + noticeMessage.getNickName() + "的好友邀请? (Y)");
                    /**
                     *          其它情况
                     * **/
                    String response = scanner.next();
                    if (!response.equals("Y")) {
                        continue;
                    } else {
                        String response1 = "";
                        Map<String, String> parameters1 = new HashMap<String, String>();
                        Request request1 = new Request();
                        /**考虑的问题：传什么参数;在线和离线返回请求的区别**/
                        boolean ifcan = true;
                        while (ifcan) {
                            if (noticeMessage.getProperty() == 0) {
                                System.out.println("\n同意还是拒绝? (Y/N)");
                                response1 = scanner.next();
                                if (response1.equals("Y")) {
                                    /**用户同意添加好友**/
                                    ifcan = false;
                                    parameters1.put("userID", userID);
                                    parameters1.put("ID", noticeMessage.getAnotherID());
                                    parameters1.put("nickName", noticeMessage.getNickName());
                                    parameters1.put("property", String.valueOf(noticeMessage.getProperty()));
                                    request1.setAll(URL_ADDRESS + "/AgreeFriend", parameters1, RequestProperty.APPLICATION);
                                    String result1 = request1.doPost();                      //返回结果"success"
                                    if (result1.equals("success"))
                                        System.out.println("添加成功!");

                                    /**
                                     *
                                     * 应该再开启一个定时循环读取最新消息的线程(包括删除)
                                     *
                                     * **/
                                } else if (response1.equals("N")) {
                                    ifcan = false;
                                } else {
                                    ifcan = true;
                                    System.out.println("输入不合法！请重新输入!");
                                    response1 = scanner.next();
                                }
                            }
                            /**
                             *              其它情况
                             * **/
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


    //获得本地消息局域网地址列表
    private static ArrayList<String> getLocalMessageAddress(int clientMessagePORT) {
        ArrayList<String> allLocalMessageAddress = new ArrayList<String>();
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            String ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = ((InetAddress) addresses.nextElement()).getHostAddress();
                    allLocalMessageAddress.add(ip + clientMessagePORT);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return allLocalMessageAddress;
    }

    //获得本地文件局域网地址列表
    private static ArrayList<String> getLocalFileAddress(int clientFilePORT) {
        ArrayList<String> allLocalFileAddress = new ArrayList<String>();
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            String ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = ((InetAddress) addresses.nextElement()).getHostAddress();
                    allLocalFileAddress.add(ip + clientFilePORT);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return allLocalFileAddress;

    }

   /* //获得localIP
    private static String getLocalIP() {
        return localIP;
    }
*/

    //开启所有线程的方法
    private static void startAllThread(String userID, DatagramSocket messageds, DatagramSocket fileds, SocketAddress messageSocketAddress, SocketAddress fileSocketAddress) throws SQLException {
        /**
         * 接收消息的线程应该为全局线程
         **/
        /*//建立接收消息的线程
        ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(messageds, messageSocketAddress);
        Thread thread = new Thread(receiveMessageThread);
        thread.start();*/
        //建立发送心跳的线程
        HeartThread heartThread = new HeartThread(userID, messageds, fileds, messageSocketAddress, fileSocketAddress);
        Thread thread1 = new Thread(heartThread);
        thread1.start();
       /* //建立监听本机局域网地址的线程
        LocalAddressThread localAddressThread = new LocalAddressThread(userID, "/findMyLocalIP");
        Thread thread2 = new Thread(localAddressThread);
        thread2.start();*/
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
        /**创建获取好友列表的线程**/
        /**
         * 应该和后面的线程组放在一起?
         * **/
        ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList");
        Thread thread6 = new Thread(contactListThread);
        thread6.start();

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
                Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type1=new TypeToken<ArrayList<String>>(){
                }.getType();
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
                } else if (message.startsWith("Chat")) {
                    String content = message.split("/")[1];
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ChatMessage>() {
                    }.getType();
                    ChatMessage chatMessage = gson.fromJson(content, type);
                    chatMessage.setNature((byte) 1);

                    String callBackID = chatMessage.getSenderID();
                    String myID = chatMessage.getAnotherID();
                    byte[] bytes = ("CallBack/" + callBackID + "/" + myID).getBytes();
                    dp = new DatagramPacket(bytes, 0, bytes.length, messageSocketAddress);
                    try {
                        ds.send(dp);                                    //应该不用再测试是否能发送成功了
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String nickName = LoginClient.contacts.get(callBackID).getNickName();                 //可以从静态变量中取得发送用户的所有基本信息
                    String text = chatMessage.getMessage();
                    String sendTime = chatMessage.getSendTime();

                    System.out.println("收到来自:" + callBackID + " 昵称为" + nickName + "的新消息:" + text + "\t发送时间:" + sendTime);
                }else if(message.startsWith("LocalChat")){
                    String content=message.split(":")[2];
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ChatMessage>() {
                    }.getType();
                    ChatMessage chatMessage = gson.fromJson(content, type);
                    chatMessage.setNature((byte) 1);

                    String callBackID=chatMessage.getSenderID();
                    String myID=chatMessage.getAnotherID();

                    String callBackAddress=message.split("/")[1];
                    ArrayList<String> addresses=gson1.fromJson(callBackAddress,type1);
                    /*Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type1=new TypeToken<ArrayList<String>>(){
                    }.getType();
                    ArrayList<String> addressList=gson.fromJson(callBackAddress)*/
                    for(String address:addresses){
                        String ip=address.split(":")[0];
                        int port= Integer.parseInt(callBackAddress.split(":")[1]);
                        SocketAddress socketAddress=new InetSocketAddress(ip,port);

                        byte[] bytes=("CallLocalBack/"+callBackID+"/"+myID).getBytes();
                        dp=new DatagramPacket(bytes,0,bytes.length,socketAddress);
                        try {
                            ds.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    String nickName=LoginClient.contacts.get(callBackID).getNickName();
                    String text = chatMessage.getMessage();
                    String sendTime = chatMessage.getSendTime();

                    System.out.println("收到来自:" + callBackID + " 昵称为" + nickName + "的新消息:" + text + "\t发送时间:" + sendTime);
                }
            }
        }
    }

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
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


/*
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
                        */
/**System.out.println(currentIP + "   " + searchIP + "   " + "客户端地址正常");**//*

                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    */
/**
 * 如何结束main()方法?
 * **//*

                    System.out.println(currentIP + "  " + searchIP + "我想结束main方法");

                    */

    /**
     * 如何重启main()方法?
     **//*

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
*/


    //监听好友中的上下线等行为(通过定时访问数据库实现)
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
                    Thread.sleep(3000);                    //睡眠5秒钟
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
                    Type type = new TypeToken<Map<String, Contact>>() {
                    }.getType();
                    Map<String, Contact> contactList = gson.fromJson(resultInfo, type);

                    /***
                     * contact即是每个联系人的Map对象，键值对为<userID,Contact>格式.
                     * 具体的应用就看客户端了
                     * 以下为模拟应用
                     * */
                    String ID;
                    String nickName;
                    byte[] headIcon;
                    byte types;
                    boolean status;
                    String theLatestText;
                    String theLatestTextTime;
                    if (contactList != null && contactList.size() > 0) {
                        System.out.println();
                        System.out.println("\n状态较刚才有更新的联系人:");
                        for (String userID : contactList.keySet()) {
                            //String status;
                            Contact contact = contactList.get(userID);
                            ID = contact.getID();
                            nickName = contact.getNickName();
                            headIcon = contact.getHeadIcon();
                            types = contact.getTypes();
                            status = contact.isStatus();
                            theLatestText = contact.getTheLatestText();
                            theLatestTextTime = contact.getTheLatestTextTime();
                            System.out.println("帐号:" + ID + " 昵称:" + nickName + " 类型:" + (types == 0 ? "好友" : "群") +
                                    (types == 0 ? (" 状态:" + (status == true ? "上线" : "下线")) : ("")) + (theLatestText == null ? (" 最后一条消息:" +
                                    theLatestText + " 消息发送时间:" + theLatestTextTime) : ""));
                            /**替换全局变量中的元素**/
                            contacts.replace(ID, contact);
                            //status = (contact.get(userID).equals("0")) ? "离线" : "在线";
                            //System.out.println("userID:" + userID + "\t" + "状态变为:" + status);
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

        public ContactListThread(String userID) {
            this.userID = userID;
        }

        public ContactListThread(String userID, String urlAddress) {
            this.userID = userID;
            this.urlAddress = urlAddress;
        }

        @Override
        public void run() {
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("userID", userID);
            Request request = new Request(URL_ADDRESS + "/ContactList", parameter, RequestProperty.APPLICATION);
            String result = "";
            result=request.doPost();
            //从resultInfo中获得每个单一的对象(resultInfo是json格式的数据)
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<Map<String, Contact>>() {
            }.getType();
            contacts = gson.fromJson(result, type);

            /***
             * contact即是每个联系人的Map对象，键值对为<userID,Contact>格式.
             * 具体的应用就看客户端了
             * 以下为模拟应用
             * */
            String ID;
            String nickName;
            byte[] headIcon;
            byte types;
            boolean status;
            String theLatestText;
            String theLatestTextTime;
            if (contacts != null && contacts.size() > 0) {
                System.out.println();
                System.out.println("\n您的联系人列表:");
                Set<String> contactList = contacts.keySet();
                Iterator<String> contactIterator = contactList.iterator();
                while (contactIterator.hasNext()) {
                    String contactID = contactIterator.next();
                    Contact contact = contacts.get(contactID);
                    ID = contact.getID();
                    nickName = contact.getNickName();
                    headIcon = contact.getHeadIcon();
                    types = contact.getTypes();
                    status = contact.isStatus();
                    theLatestText = contact.getTheLatestText();
                    theLatestTextTime = contact.getTheLatestTextTime();
                    System.out.println("帐号:" + ID + " 昵称:" + nickName + " 类型:" + (types == 0 ? "好友" : "群") +
                            (types == 0 ? (" 状态:" + (status == true ? "上线" : "下线")) : ("")) + (theLatestText == null ? (" 最后一条消息:" +
                            theLatestText + " 消息发送时间:" + theLatestTextTime) : ""));
                }
                System.out.println();
            } else {
                System.out.println("暂无联系人!");
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
            while (true) {
                this.notices = "";
                this.notices = this.request.doPost();                             //获得收到的消息提醒
                if (notices.equals("") || notices.equals("none")) {
                    //System.out.println("您离线时未收到过任何(如添加好友)请求!");
                } else {
                    /**解析Json**/
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ArrayList<NoticeMessage>>() {
                    }.getType();
                    noticeMessages = gson.fromJson(this.notices, type);
                    for (NoticeMessage noticeMessage : noticeMessages) {
                        String anotherID = noticeMessage.getAnotherID();
                        String nickName = noticeMessage.getNickName();
                        byte property = noticeMessage.getProperty();
                        if (property == 0)                                             //好友邀请
                            System.out.println("\n来自帐号:" + anotherID + "  昵称为" + nickName + " 的用户的好友邀请");
                        else if (property == 1)
                            System.out.println("1");
                        else if (property == 2)
                            System.out.println("\n添加好友:" + anotherID + "  昵称:" + nickName + "  成功");
                        else if (property == 3)
                            System.out.println("3");
                    }
                }
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            if (notices.equals("") || notices.equals("none")) {
                System.out.println("\n您离线时未收到过任何消息!");
            } else {
                /**解析Json**/
            }
        }
    }

    //发送消息的线程
    //在此线程中停止接收方法
    private final static class ChatSendTask implements Callable<Boolean> {
        private DatagramSocket ds;
        private SocketAddress socketAddress;
        private String senderID;
        private String anotherID;
        private byte nature;
        private String sendTime;
        private String messsage;
        private DatagramPacket dp = null;
        private byte[] bytes = null;
        private ChatMessage chatMessage;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

        public ChatSendTask(DatagramSocket messageds, SocketAddress messageSocketAddress, String senderID, String anotherID, byte nature, String sendTime, String message) {
            this.ds = messageds;
            this.socketAddress = messageSocketAddress;
            this.senderID = senderID;
            this.anotherID = anotherID;
            this.nature = nature;
            this.sendTime = sendTime;
            this.messsage = message;
            this.chatMessage = new ChatMessage(senderID, anotherID, nature, sendTime, message);
        }

        @Override
        public Boolean call() throws Exception {
            String content = gson.toJson(chatMessage);
            bytes = ("Chat/" + content).getBytes();
            dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] receby = new byte[1024 * 6];
            dp = new DatagramPacket(receby, 0, receby.length);
            try {
                do {
                    ds.receive(dp);
                } while (!(new String(dp.getData(), 0, dp.getLength()).startsWith("CallBack")));
                System.out.println("成功回收数据!信息发送成功!" + new String(dp.getData(), 0, dp.getLength()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private static class ChatLocalSendTask implements Callable<Boolean>{
        private DatagramSocket ds;
        private String senderID;
        private String anotherID;
        private SocketAddress socketAddress;
        private byte nature;
        private String sendTime;
        private String messsage;
        private DatagramPacket dp = null;
        private byte[] bytes = null;
        private ChatMessage chatMessage;
        private String address;
        private String addressList;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

        public ChatLocalSendTask(DatagramSocket messageds, String senderID, String anotherID, byte nature, String sendTime, String message,String address,String addressList) {
            this.ds = messageds;
            this.senderID = senderID;
            this.anotherID = anotherID;
            this.nature = nature;
            this.sendTime = sendTime;
            this.messsage = message;
            this.address=address;
            this.addressList=addressList;
            this.chatMessage = new ChatMessage(senderID, anotherID, nature, sendTime, message);
        }

        @Override
        public Boolean call() throws Exception {
            /*Map<String,String> parameters=new HashMap<String, String>();
            parameters.put("senderID",senderID);
            parameters.put("anotherID",anotherID);
            Request request=new Request(LoginClient.URL_ADDRESS+"/GetLocalAddress",parameters,RequestProperty.APPLICATION);
            String addressList=request.doPost();
            String address=addressList.split("/")[1];*/
            String ip=address.split(":")[0];
            int port= Integer.parseInt(address.split(":")[1]);

            socketAddress=new InetSocketAddress(ip,port);
            String content = gson.toJson(chatMessage);
            bytes = ("LocalChat/" + addressList + "/"+content).getBytes();
            dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] receby = new byte[1024 * 6];
            dp = new DatagramPacket(receby, 0, receby.length);
            try {
                do {
                    ds.receive(dp);
                } while (!(new String(dp.getData(), 0, dp.getLength()).startsWith("CallLocalBack")));
                System.out.println("成功回收数据!局域网内信息发送成功!" + new String(dp.getData(), 0, dp.getLength()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private static class WaitThread implements Runnable{
        private Thread thread;

        public WaitThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            synchronized (thread){
                try {
                    thread.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static class NotifyThread implements Runnable{
        private Thread thread;

        public NotifyThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            synchronized (thread) {
                thread.notify();
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