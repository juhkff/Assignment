package wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.contact.Contact;
import model.file.FileProgress;
import model.group.GroupMessage;
import model.group.SimpleGroup;
import model.message.ChatMessage;
import model.message.FileMessage;
import model.message.NoticeMessage;
import test.Client.Request;
import test.Client.RequestProperty;
import tools.Chat;
import wrapper.test.TestBlockingThread;
import wrapper.test.TestFileMessageThread;
import wrapper.test.TestFileProgressThread;
import wrapper.thread.update.UpdateLocalFriendSortThread;
import wrapper.thread.update.UpdateLocalFriendThread;
import wrapper.thread.update.UpdateLocalGroupSortThread;
import wrapper.thread.update.UpdateLocalGroupThread;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.*;

public class ThreadWrapper {

    //开启所有线程的方法
    public final static void startAllThread(String userID, String exitTime, BlockingQueue<ChatMessage> chatMessages,
                                            BlockingQueue<FileProgress> fileProgresses,BlockingQueue<FileMessage> fileMessages,
                                            BlockingQueue<NoticeMessage> noticeMessages,BlockingQueue<ChatMessage> files, BlockingQueue<GroupMessage> groupMessages) throws SQLException, InterruptedException
    {


        //向服务器发送本地局域网地址
        SocketWrapper.sendLocalAddress(userID);

        /**
         * 接收消息的线程应该为全局线程
         **/
        //建立接收消息的线程
        ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(/*messageds, fileds, messageSocketAddress,*/ userID,chatMessages,fileMessages);
        Thread thread0 = new Thread(receiveMessageThread);
        thread0.start();



        //建立显示消息的线程
        TestBlockingThread testBlockingThread=new TestBlockingThread(chatMessages);
        Thread thread212=new Thread(testBlockingThread);
        thread212.start();

        //建立显示进度条的线程
        TestFileProgressThread testFileProgress=new TestFileProgressThread(fileProgresses);
        Thread thread222=new Thread(testFileProgress);
        thread222.start();

        //建立显示在线发送文件的请求的线程
        TestFileMessageThread testFileMessageThread=new TestFileMessageThread(fileMessages,fileProgresses);
        Thread thread223=new Thread(testFileMessageThread);
        thread223.start();


        //建立发送心跳的线程
        HeartThread heartThread = new HeartThread(userID/*, messageds, fileds, messageSocketAddress, fileSocketAddress*/);
        Thread thread1 = new Thread(heartThread);
        thread1.start();

/*        *//**创建获取好友列表的线程**//*
        ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList"*//*, contacts*//*);
        Thread thread6 = new Thread(contactListThread);
        thread6.start();
        thread6.join();                                                             *//**这个join应该管用**//*

        *//**创建获取群列表的线程**//*
        GroupListThread groupListThread = new GroupListThread(userID*//*, groups, URL_ADDRESS*//*);
        Thread thread7 = new Thread(groupListThread);
        thread7.start();
        thread7.join();*/

        /*建立一次性线程接收最新消息提醒(就是用户刚上线时的消息提醒)
          以及
          建立一次性线程接收最新聊天消息提醒*/
        MessageNoticeThread messageNoticeThread = new MessageNoticeThread(userID,noticeMessages/*, URL_ADDRESS, noticeMessages, contacts*/);
//        ChatNoticeThread chatNoticeThread = new ChatNoticeThread(userID/*, URL_ADDRESS, contacts*/);
        Thread thread4 = new Thread(messageNoticeThread);
//        Thread thread5 = new Thread(chatNoticeThread);
        thread4.start();
//        thread4.join();
//        thread5.start();
//        thread5.join();

        /**对每个群都建立一个监听群消息的线程**/
        for (int i = 0; i < groups.size(); i++) {
            Set<String> groupIDList = groups.keySet();
            Iterator<String> iterator = groupIDList.iterator();
            while (iterator.hasNext()) {
                String groupID = iterator.next();
                GroupListenerThread groupListenerThread = new GroupListenerThread(userID, groupID, exitTime, groupMessages/*, URL_ADDRESS, ifbreak, groups*/);
                Thread thread = new Thread(groupListenerThread);
                thread.start();
            }
        }

        /**建立用户全群监听线程**/
        AllGroupListenerThread allGroupListenerThread = new AllGroupListenerThread(userID/*, URL_ADDRESS, groups*/);
        Thread thread21 = new Thread(allGroupListenerThread);
        thread21.start();


        //建立查询servlet获得状态有更新的好友的数据
        ListenerThread listenerThread = new ListenerThread(userID, URL_ADDRESS + "/Listener");
        Thread thread3 = new Thread(listenerThread);
        thread3.start();


        //建立监听新消息的线程
        ContactListenerThread contactListenerThread=new ContactListenerThread(userID,exitTime,chatMessages,files);
        Thread thread=new Thread(contactListenerThread);
        thread.start();

    }


    //监听服务器发来的更改文件的线程
    public final static class ListenerServerSocket implements Runnable{
        private Socket socket;
        private FileOutputStream fileOutputStream;
        private InputStream inputStream;
        private DataInputStream dataInputStream;
        private String command;

        @Override
        public void run() {
            try {
                socket=new Socket(FILE_SERVER_IP,FILE_SERVER_SOCKET_JSON_PORT);
                OutputStream outputStream=socket.getOutputStream();
                DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                dataOutputStream.writeUTF("Listener");
                while (true) {
                    inputStream=socket.getInputStream();
                    dataInputStream=new DataInputStream(inputStream);
                    command=dataInputStream.readUTF();
                    if (command.equals("Friends")){
                        UpdateLocalFriendThread updateLocalFriendThread=new UpdateLocalFriendThread(socket,inputStream);
                        Thread thread=new Thread(updateLocalFriendThread);
                        thread.start();
                    }else if (command.equals("Groups")){
                        UpdateLocalGroupThread updateLocalGroupThread=new UpdateLocalGroupThread(socket,inputStream);
                        Thread thread=new Thread(updateLocalGroupThread);
                        thread.start();
                    }else if (command.equals("SortFriends")){
                        UpdateLocalFriendSortThread updateLocalFriendSortThread=new UpdateLocalFriendSortThread(socket,inputStream);
                        Thread thread=new Thread(updateLocalFriendSortThread);
                        thread.start();
                    }else if (command.equals("SortGroups")){
                        UpdateLocalGroupSortThread updateLocalGroupSortThread=new UpdateLocalGroupSortThread(socket,inputStream);
                        Thread thread=new Thread(updateLocalGroupSortThread);
                        thread.start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //接收消息的线程类
    public final static class ReceiveMessageThread implements Runnable {
        private byte[] by;
        private String message;
        private String userID;
        private BlockingQueue<ChatMessage> chatMessages;
        private DatagramPacket dp;
        private BlockingQueue<FileMessage> fileMessages;
        public ReceiveMessageThread(String userID,BlockingQueue<ChatMessage> chatMessages,BlockingQueue<FileMessage> fileMessages) {
            by = new byte[1024 * 8];
            this.userID = userID;
            this.chatMessages=chatMessages;
            this.fileMessages=fileMessages;
        }

        @Override
        public void run() {
            /*接收消息的线程启动!*/
            System.out.println("等待接收...");
            while (true) {
                by = new byte[1024 * 8];
                dp = new DatagramPacket(by, 0, by.length);
                try {
                    messageds.receive(dp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /**
                 * ------------------------------------对收到的dp包进行各种情况的讨论--------------------------------
                 * **/


                Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type1 = new TypeToken<ArrayList<String>>() {
                }.getType();
                message = new String(dp.getData(), 0, dp.getLength());
                if (message.equals("PublicAddressChanged")) {
                    System.out.println("登录地址异常!");
                } else if (message.startsWith("Chat")) {
                    String content = message.split("/")[2];
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ChatMessage>() {
                    }.getType();
                    ChatMessage chatMessage = gson.fromJson(content, type);
                    chatMessage.setNature((byte) 1);
                    String callBackID = chatMessage.getSenderID();
                    String myID = userID;
                    byte[] bytes = ("CallBack/" + message.split("/")[1] + "/" + callBackID + "/" + myID).getBytes();
                    dp = new DatagramPacket(bytes, 0, bytes.length, messageSocketAddress);
                    try {
                        messageds.send(dp);                                    //应该不用再测试是否能发送成功了
                        chatMessage.setMessage(Chat.decodeChinese(chatMessage.getMessage()));
                        //将新消息chatMessage对象添加到全局变量chatMessages中
                        chatMessages.put(chatMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (message.startsWith("CallBack")) {
                    int index = Integer.parseInt(message.split("/")[1]);
                    ifis.replace(index, true);
                    System.out.println("外网发送信息回复:From " + message.split("/")[2] + " To " + message.split("/")[3] + " :Success");
                } else if (message.startsWith("LocalChat")) {
                    String content = message.split("/")[2];
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ChatMessage>() {
                    }.getType();
                    ChatMessage chatMessage = gson.fromJson(content, type);
                    chatMessage.setNature((byte) 1);

                    String callBackID = chatMessage.getAnotherID();
                    //String myID = chatMessage.getAnotherID();
                    String myID = this.userID;

                    String callBackAddress = message.split("/")[1];
                    ArrayList<String> addresses = gson1.fromJson(callBackAddress, type1);
                    for (String address : addresses) {
                        String ip = address.split(",")[0];
                        int port = Integer.parseInt(address.split(",")[1]);
                        SocketAddress socketAddress = new InetSocketAddress(ip, port);

                        byte[] bytes = ("CallLocalBack/" + callBackID + "/" + myID).getBytes();
                        dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
                        try {
                            messageds.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    String nickName = contacts.get(callBackID).getNickName();
                    String text = null;
                    try {
                        text = chatMessage.getMessage();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String sendTime = chatMessage.getSendTime();

                    System.out.println("收到来自:" + callBackID + " 昵称为" + nickName + "的新消息:" + text + "\t发送时间:" + sendTime);
                } else if (message.startsWith("OnlineTransmit")) {
                    String receiverAddr = message.split("/")[2];                          //接收方的地址
                    String addrlist = message.split("/")[3];                              //发送方的地址列表
                    ArrayList<String> addresses = gson1.fromJson(addrlist, type1);
                    for (String address : addresses) {
                        String ip = address.split(",")[0];
                        int port = Integer.parseInt(address.split(",")[1]);
                        SocketAddress socketAddress = new InetSocketAddress(ip, port);

                        byte[] bytes = ("Admitted/" + message.split("/")[1] + "/" + address + "/" + receiverAddr).getBytes();
                        dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
                        try {
                            messageds.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (message.startsWith("SendFile")) {
                    /**接收到在线发送文件的请求**/
                    ResponseThread responseThread = new ResponseThread(dp,fileMessages);
                    Thread thread = new Thread(responseThread);
                    thread.start();
                } else if (message.startsWith("Admitted")) {
                    int index = Integer.parseInt(message.split("/")[1]);
                    ifis.replace(index, true);
                    senderFileAddress.put(index, message.split("/")[2]);
                    receiverFileAddress.put(index, message.split("/")[3]);
                }
            }
        }

        private class ResponseThread implements Runnable {
            private DatagramPacket dp;
            private String message;
            private FileMessage fileMessage;
            private BlockingQueue<FileMessage> fileMessages;
            public ResponseThread(DatagramPacket dp,BlockingQueue<FileMessage> fileMessages) {
                this.dp = dp;
                this.message = new String(this.dp.getData(), 0, this.dp.getLength());
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<FileMessage>() {
                }.getType();
                this.fileMessage = gson.fromJson(message.substring(9, message.length()), type);
                this.fileMessages=fileMessages;
            }

            @Override
            public void run() {
                try {
                    fileMessages.put(fileMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //发送心跳包
    public final static class HeartThread implements Runnable {
        private String userID;
        private byte[] by;                      //by数组毫无用处，仅仅是为了初始化DatagramPacket对象而存在
        private DatagramPacket messdp;
        private DatagramPacket fildp;
        public HeartThread(String userID) {
            this.userID = userID;
            this.by = ("Heartbeat:" + this.userID).getBytes();
            messdp=new DatagramPacket(by,0,by.length,messageSocketAddress);
            fildp=new DatagramPacket(by,0,by.length,fileSocketAddress);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    messageds.send(messdp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fileds.send(fildp);
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

    //监听好友(非群)中的上下线等行为(通过定时访问数据库实现)
    public final static class ListenerThread implements Runnable {
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
                    Thread.sleep(3000);                    //睡眠3秒钟
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
                            System.out.println("帐号:" + ID + " 昵称:" + nickName + "头像: (无法显示)" + " 类型:" + (types == 0 ? "好友" : "群") +
                                    (types == 0 ? (" 状态:" + (status == true ? "上线" : "下线")) : ("")) + (theLatestText != null ? (" 最后一条消息:" +
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
    public final static class ContactListThread implements Runnable {
        private String userID;
        private String urlAddress;
        private HttpURLConnection uRLConnection;
        private URL url;
        private String resultInfo;
//        private Map<String, Contact> contacts;

        public ContactListThread(String userID) {
            this.userID = userID;
        }

        public ContactListThread(String userID, String urlAddress/*, Map<String, Contact> contacts*/) {
            this.userID = userID;
            this.urlAddress = urlAddress;
//            this.contacts = contacts;
        }

        @Override
        public void run() {
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("userID", userID);
            Request request = new Request(this.urlAddress, parameter, RequestProperty.APPLICATION);
            String result = "";
            result = request.doPost();
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
                    theLatestText = null;
                    try {
                        theLatestText = contact.getTheLatestText();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    theLatestTextTime = contact.getTheLatestTextTime();
                    System.out.println("帐号:" + ID + " 昵称:" + nickName + "头像:(忽略)" + " 类型:" + (types == 0 ? "好友" : "群") +
                            (types == 0 ? (" 状态:" + (status == true ? "上线" : "下线")) : ("")) + (theLatestText != null ? (" 最后一条消息:" +
                            theLatestText + " 消息发送时间:" + theLatestTextTime) : ""));
                }
                System.out.println();
            } else {
                System.out.println("暂无联系人!");
            }
        }
    }

    //获取群列表的线程
    public final static class GroupListThread implements Runnable {
        private Map<String, SimpleGroup> simpleGroupArrayList = new HashMap<String, SimpleGroup>();
        private String userID;
//        private Map<String, SimpleGroup> groups;
//        private String URL_ADDRESS;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Request request;
        private String groupsTrans;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private Type type = new TypeToken<Map<String, SimpleGroup>>() {
        }.getType();

        public GroupListThread(String userID/*, Map<String, SimpleGroup> groups, String URL_ADDRESS*/) {
            this.userID = userID;
//            this.groups = groups;
//            this.URL_ADDRESS = URL_ADDRESS;
            parameters.put("userID", this.userID);
            this.request = new Request(URL_ADDRESS + "/GroupList", parameters, RequestProperty.APPLICATION);
        }

        @Override
        public void run() {
            this.groupsTrans = this.request.doPost();
            if (!groupsTrans.equals("none")) {
                //获得群集合
                this.simpleGroupArrayList = this.gson.fromJson(groupsTrans, type);
            } else
                this.simpleGroupArrayList = null;
            //groups = this.simpleGroupArrayList;
            if (/*contacts*/this.simpleGroupArrayList != null) {
                groups = this.simpleGroupArrayList;
                System.out.println("\n您的群列表\n");
                Set<String> simpleGroupSet = this.simpleGroupArrayList.keySet();
                Iterator<String> stringIterator = simpleGroupSet.iterator();
                while (stringIterator.hasNext()) {
                    String groupID = stringIterator.next();
                    SimpleGroup simpleGroup = this.simpleGroupArrayList.get(groupID);

                    System.out.println("群ID: " + simpleGroup.getGroupID() +
                            "群昵称: " + simpleGroup.getGroupName() +
                            "\t群头像:" +/*simpleGroup.getGroupIcon()*/"无法显示" +
                            "\t群最新消息:" + simpleGroup.getTheLatestMessage() +
                            "\t最新消息发送时间: " + simpleGroup.getTheLatestSendTime()
                    );
                }
            } else {
                System.out.println("群列表为空!");
            }
        }
    }

    //获取消息提醒的线程
    public final static class MessageNoticeThread implements Runnable {
        private String userID;
        private Map<String, String> parameter = new HashMap<String, String>();
        private Request request;
        private String notices;                                                 //收到的消息提醒
        private ArrayList<NoticeMessage> noticeMessages;
        private BlockingQueue<NoticeMessage> noticeMessageBlockingQueue;
        public MessageNoticeThread(String userID,BlockingQueue<NoticeMessage> noticeMessages) {
            this.userID = userID;
            this.parameter.put("userID", this.userID);
            this.request = new Request(URL_ADDRESS + "/GetNotice", this.parameter, RequestProperty.APPLICATION);
            this.noticeMessageBlockingQueue=noticeMessages;
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
//                    StaticVariable.noticeMessages=noticeMessages;
                    for (NoticeMessage noticeMessage : noticeMessages) {
                        try {
                            noticeMessageBlockingQueue.put(noticeMessage);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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

    //(每个群)监听群消息更新的线程
    public final static class GroupListenerThread implements Runnable {
        private String userID;
        private String groupID;
        private String referredTime;
//        private String URL_ADDRESS;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Request request;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private Type type = new TypeToken<ArrayList<GroupMessage>>() {
        }.getType();
        private ArrayList<GroupMessage> groupMessageArrayList = null;
        private BlockingQueue<GroupMessage> groupMessages;
//        private Map<String, Boolean> ifbreak;
//        private Map<String, SimpleGroup> groups;

        public GroupListenerThread(String userID, String groupID, String exitTime, BlockingQueue<GroupMessage> groupMessages/*, String URL_ADDRESS, Map<String, Boolean> ifbreak, Map<String, SimpleGroup> groups*/) {
            this.userID = userID;
            this.groupID = groupID;
            this.referredTime = exitTime;
//            this.URL_ADDRESS = URL_ADDRESS;
            this.parameters.put("userID", userID);
            this.parameters.put("groupID", groupID);
            this.parameters.put("referredTime", referredTime);
            this.request = new Request(URL_ADDRESS + "/GroupListener", parameters, RequestProperty.APPLICATION);
//            this.ifbreak = ifbreak;
            /**向全局变量ifbreak中添加此群的存在判断键值对**/
            ifbreak.put(this.groupID, true);
            this.groupMessages=groupMessages;
//            this.groups = groups;
        }

        @Override
        public void run() {
            String result = null;
            while (true) {
                /**用户退出该群时，会将ifbreak中该群对应的值改为false，若检查为false，则退出此线程**/
                if (!ifbreak.get(this.groupID))
                    break;
                result = null;
                this.request = new Request(URL_ADDRESS + "/GroupListener", this.parameters, RequestProperty.APPLICATION);
                groupMessageArrayList = null;
                result = this.request.doPost();
                if (!result.equals("none")) {
                    /**获得消息列表**/
                    groupMessageArrayList = gson.fromJson(result, type);
                    System.out.println("\n收到新的群聊消息!");
                    for (GroupMessage groupMessage : groupMessageArrayList) {
                        try {
                            /**转换编码**/
                            groupMessage.setContent(Chat.decodeChinese(groupMessage.getContent()));
                            groupMessages.put(groupMessage);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            System.out.println("error in LoginClient GroupListenerThread !");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    /*更改groups全局变量中的最新消息以及最新消息发送时间*/
                    groups.get(groupID).setTheLatestMessage(groupMessageArrayList.get(groupMessageArrayList.size() - 1).getContent());
                    groups.get(groupID).setTheLatestSendTime(groupMessageArrayList.get(groupMessageArrayList.size() - 1).getSendTime());

                    /**更换参考时间为接收到的消息的最新时间**/
                    parameters.replace("referredTime", groupMessageArrayList.get(groupMessageArrayList.size() - 1).getSendTime());

                    /**以下代码是为了让程序能够在控制台上显示出变化加的额外程序，实际中应该不需要**//*
                    Map<String, String> parameters2 = new HashMap<String, String>();
                    parameters2.put("userID", userID);
                    parameters2.put("groupID", groupID);
                    Request request2 = new Request(URL_ADDRESS + "/UpdateGroupInfo", parameters2, RequestProperty.APPLICATION);
                    String result2 = request2.doPost();*/

                } else if (result.equals("none")) {
                    //System.out.println("暂无新消息!");
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("error when GroupListenerThread sleep !");
                }
            }
        }
    }

    //监听所有群更新情况的线程
    public final static class AllGroupListenerThread implements Runnable {
        private String userID;
//        private String URL_ADDRESS;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Request request;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private Type type = new TypeToken<Map<String, SimpleGroup>>() {
        }.getType();
        private String result;
//        private Map<String, SimpleGroup> groups;

        public AllGroupListenerThread(String userID/*, String URL_ADDRESS, Map<String, SimpleGroup> groups*/) {
            this.userID = userID;
//            this.URL_ADDRESS = URL_ADDRESS;
//            this.groups = groups;
            this.parameters.put("userID", this.userID);
        }

        @Override
        public void run() {
            while (true) {
                this.request = null;
                this.request = new Request(URL_ADDRESS + "/ListenerForAllGroups", parameters, RequestProperty.APPLICATION);
                result = null;
                result = this.request.doPost();
                if (!result.equals("none")) {
                    Map<String, SimpleGroup> simpleGroupMap = gson.fromJson(result, type);
                    /**获得更新群SimpleGroup列表**/
                    System.out.println("\n联系人中有更新的群列表:");
                    Set<String> groupSet = simpleGroupMap.keySet();
                    Iterator<String> iterator = groupSet.iterator();
                    String groupID;
                    while (iterator.hasNext()) {
                        groupID = iterator.next();
                        SimpleGroup simpleGroup = simpleGroupMap.get(groupID);
                        try {
                            /**转换编码**/
                            simpleGroup.setTheLatestMessage(Chat.decodeChinese(simpleGroup.getTheLatestMessage()));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        System.out.println("群ID:" + simpleGroup.getGroupID() +
                                "\t群昵称: " + simpleGroup.getGroupName() +
                                "\t群头像: " +/*simpleGroup.getGroupIcon()*/"(无法显示)" +
                                "\t最后一条消息: " + simpleGroup.getTheLatestMessage() +
                                "\t最后消息发送时间: " + simpleGroup.getTheLatestSendTime()
                        );

                        /**用此SimpleGroup对象去替换全局变量**/
                        groups.replace(groupID, simpleGroup);
                    }
                } else {
                    /**无群更新**/
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //发送消息的线程
    public final static class SendThread implements Runnable {
        private String senderID;
        private String anotherID;
        private byte nature;
        private String sendTime;
        private String message;
        private DatagramPacket dp = null;
        private DatagramPacket recevdp = null;
        private byte[] receibytes = new byte[1024 * 6];
        private byte[] bytes = null;
        private ChatMessage chatMessage;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private int index;

        public SendThread(int Thread_Index,String senderID, String anotherID, byte nature, String sendTime, String message) {
            this.senderID = senderID;
            this.anotherID = anotherID;
            this.nature = nature;
            this.sendTime = sendTime;
            this.message = message;
            this.chatMessage = new ChatMessage(senderID, anotherID, nature, sendTime, message);
            this.index=Thread_Index;
        }

        @Override
        public void run() {
            String content = gson.toJson(chatMessage);
            bytes = ("Chat/" + this.index + "/" + content).getBytes();
            dp = new DatagramPacket(bytes, 0, bytes.length, messageSocketAddress);
            try {
                messageds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!ifis.get(this.index)) {          /** ==false **/
                System.out.println("外网发送无效，尝试数据库离线式发送...");                 /**其实是更新数据库的状态栏**/
                Map<String, String> parameters3 = new HashMap<String, String>();
                parameters3.put("userID", senderID);
                parameters3.put("anotherID", anotherID);
                parameters3.put("message", message);
                parameters3.put("sendTime", sendTime);
                Request request3 = new Request(URL_ADDRESS + "/updateContactStatus", parameters3, RequestProperty.APPLICATION);
                String result3 = request3.doPost();               //result2 : success / false;
                System.out.println(result3);
            } else {
                System.out.println("外网发送成功!");
            }
            ifis.remove(this.index);
        }
    }

    //监听好友新消息的线程
    public final static class ContactListenerThread implements Runnable{
        private Map<String,String> parameters=new HashMap<String, String>();
        private Request request;
        private BlockingQueue<ChatMessage> chatMessages;
        private BlockingQueue<ChatMessage> files;
        public ContactListenerThread(String userID,String exitTime,BlockingQueue<ChatMessage> chatMessages,BlockingQueue<ChatMessage> files){
            this.parameters.put("userID",userID);
            this.parameters.put("updateTime",exitTime);
            this.request=new Request(URL_ADDRESS+"/ReceiveChat",this.parameters,RequestProperty.APPLICATION);
            this.chatMessages=chatMessages;
            this.files=files;
        }

        @Override
        public void run() {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<ArrayList<ChatMessage>>() {
            }.getType();
            while (true) {
                String result = request.doPost();
                ArrayList<ChatMessage> chatMessageArrayList = gson.fromJson(result, type);
                if (chatMessageArrayList.size() == 0) {
//                    System.out.println("无");
                } else {
                    System.out.println("有新消息!");
                    for (ChatMessage chatMessage : chatMessageArrayList) {

                        //放到存储库中
                        try {
                            chatMessage.setMessage(Chat.decodeChinese(chatMessage.getMessage()));                        /**发送方的头像不是从ChatMessage类中获得的，而是从联系人列表中摘取的**/
                            chatMessages.put(chatMessage );
                            if (chatMessage.getNature()==2||chatMessage.getNature()==3||chatMessage.getNature()==7||chatMessage.getNature()==8){
                                files.put(chatMessage);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    ChatMessage chatMessage = chatMessageArrayList.get(chatMessageArrayList.size() - 1);
                    this.parameters.replace("updateTime", chatMessage.getSendTime());
                    this.request=new Request(URL_ADDRESS+"/ReceiveChat",this.parameters,RequestProperty.APPLICATION);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
