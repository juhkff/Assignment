package test.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.contact.Contact;
import model.file.FileProgress;
import model.file.MyFileInfo;
import model.group.Group;
import model.group.GroupMember;
import model.group.GroupMessage;
import model.group.SimpleGroup;
import model.message.ChatMessage;
import model.message.FileMessage;
import model.message.NoticeMessage;
import model.property.User;
import tools.Chat;
import tools.DateTime;
import tools.file.File;
import tools.file.model.FileSenderThread;
import tools.file.model.UDPUtils;
import wrapper.*;
import wrapper.group.GroupWrapper;
import wrapper.thread.OnlineTransThread;
import wrapper.thread.ReceiveFileThread;
import wrapper.thread.TransFileThread;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.*;

public class LoginClient {

    public static void main(String[] args) throws Exception {
        //String userID = "2461247724";               //juhkff
        String userID = "8133523681";               //juhkgf
        String passWord = "aqko251068";

        /**登录工作过程**/
        String nickName;
        byte[] headIcon;
        String exitTime;


        Scanner scanner = new Scanner(System.in);
        String result = wrapper.LoginWrapper.login(userID, passWord);
        User user = null;
        if (result.equals("null"))
            System.out.println("帐号不存在，登录失败!");
        else if (result.equals("false"))
            System.out.println("密码错误，登录失败!");
        else if (result.equals("error"))
            System.out.println("发生了未知错误...");
        else if (result.equals("true")) {
            System.out.println("帐号密码验证成功!");
            user = LoginWrapper.getUser(userID, passWord);
            System.out.println("获得用户信息!");
            /**获取用户的nickName属性**/
            nickName = user.getNickName();
            headIcon = user.getHeadIcon();
            exitTime = user.getExitTime();


            BlockingQueue<ChatMessage> chatMessages=new ArrayBlockingQueue<ChatMessage>(1);                     //接收到线程
            BlockingQueue<FileProgress> fileProgresses=new ArrayBlockingQueue<FileProgress>(1);                 //接收到文件接收时的进度条，理论上只能同时接收和发送一个文件
            BlockingQueue<FileMessage> fileMessages=new ArrayBlockingQueue<FileMessage>(1);
            BlockingQueue<NoticeMessage> noticeMessages=new ArrayBlockingQueue<NoticeMessage>(1);
            BlockingQueue<ChatMessage> files=new ArrayBlockingQueue<ChatMessage>(1);
            BlockingQueue<GroupMessage> groupMessages=new ArrayBlockingQueue<GroupMessage>(1);

            //创建所有线程
            ThreadWrapper.startAllThread(userID,exitTime,chatMessages,fileProgresses,fileMessages,noticeMessages,files,groupMessages);

            /**---------------------------------以下情景为模拟用户使用程序，注意聊天应自动建立线程(不建立线程则无法实现多窗口聊天)--------------------------------**/

            String nextCommand;
            while (true) {
                /**交互指令**/
                System.out.println("\n聊天/Chat\t进入群操作界面/Groups\t添加好友/Add\t添加群/AddGroup退出程序/Exit\t处理请求/Deal\t上传离线文件/Submit\t创建群/CreateGroup\t更改个人信息/UpdateInfo");
                System.out.print("输入 您要进行的操作 :______\b\b\b\b\b\b");
                nextCommand = scanner.nextLine();

                if (nextCommand.equals("Chat")) {
                    /**与聊天有关的操作**/
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

                    /**本模拟程序只能模拟与一个用户聊天的过程，实际应该可以与多个用户**/

                    ArrayList<ChatMessage> historyChatMessages = ChatWrapper.getSimpleChat(userID, anotherID);   //historyChatMessages存储聊天记录
                    int size = historyChatMessages.size();
                    if (size > 7)
                        size = 5;                                                                                        //历史记录过多（大于7）则只输出后5条
                    for (int index = historyChatMessages.size() - size; index < historyChatMessages.size(); index++) {
                        ChatMessage chatMessage = historyChatMessages.get(index);
                        if (chatMessage.getNature() == 1)                   //1:他向我说; 0:我向他说
                            System.out.println("" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                        else if (chatMessage.getNature() == 0)
                            System.out.println("\t\t\t\t\t\t" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                        else if (chatMessage.getNature() == 2)
                            System.out.println("\t\t\t\t\t\t文件:" + chatMessage.getMessage() + " sendTime : " + chatMessage.getSendTime());
                        else if (chatMessage.getNature() == 3)
                            System.out.println("文件:" + chatMessage.getMessage() + " sendTime : " + chatMessage.getSendTime());
                        else if (chatMessage.getNature() == 5)
                            System.out.println("\t\t\t\t\t\t图片:" + chatMessage.getImg() + " sendTime : " + chatMessage.getSendTime());
                        else if (chatMessage.getNature() == 6)
                            System.out.println("图片" + chatMessage.getImg() + " sendTime : " + chatMessage.getSendTime());
                    }
                    //DateTime dateTime = new DateTime();
                    byte nature = 0;
                    while (true) {
                        /**互动操作**/
                        System.out.println("\n发送消息(Exit退出):\tImg/发送图片\tSubmit/发送离线文件\tReceive/接收离线文件\tOnlineTransmit/发送在线文件\tHistory/聊天记录\t");
                        scanner.nextLine();
                        String message = scanner.nextLine();
                                                                        if (message.equals("Exit"))
                            break;
                        else if (message.equals("History")) {
                            /**获取聊天记录**/
                            System.out.println("聊天记录");
                            int num = historyChatMessages.size();
                            if (num <= 10) {
                                for (ChatMessage chatMessage : historyChatMessages) {
                                    if (chatMessage.getNature() == 1)                   //1:他向我说; 0:我向他说
                                        System.out.println("" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                                    else if (chatMessage.getNature() == 0)
                                        System.out.println("\t\t\t\t\t\t" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                                    else if (chatMessage.getNature() == 2)
                                        System.out.println("\t\t\t\t\t\t文件:" + chatMessage.getMessage() + " sendTime : " + chatMessage.getSendTime());
                                    else if (chatMessage.getNature() == 3)
                                        System.out.println("文件:" + chatMessage.getMessage() + " sendTime : " + chatMessage.getSendTime());
                                    else if (chatMessage.getNature() == 5)
                                        System.out.println("\t\t\t\t\t\t图片:" + chatMessage.getImg() + " sendTime : " + chatMessage.getSendTime());
                                    else if (chatMessage.getNature() == 6)
                                        System.out.println("图片" + chatMessage.getImg() + " sendTime : " + chatMessage.getSendTime());
                                }
                            } else {
                                int j = 0;
                                String resp = "next";
                                do {
                                    if (!resp.equals("next")) {
                                        System.out.println("格式错误，请重新输入!");
                                        resp = scanner.next();
                                        continue;
                                    }
                                    j++;
                                    int k = 10 * (j - 1);
                                    for (; k < 10 * j && k < historyChatMessages.size(); k++) {
                                        ChatMessage chatMessage = historyChatMessages.get(k);
                                        if (chatMessage.getNature() == 1)                   //1:他向我说; 0:我向他说
                                            System.out.println("" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                                        else if (chatMessage.getNature() == 0)
                                            System.out.println("\t\t\t\t\t\t" + chatMessage.getSendTime() + " : " + chatMessage.getMessage());
                                        else if (chatMessage.getNature() == 2)
                                            System.out.println("\t\t\t\t\t\t文件:" + chatMessage.getMessage() + " sendTime : " + chatMessage.getSendTime());
                                        else if (chatMessage.getNature() == 3)
                                            System.out.println("文件:" + chatMessage.getMessage() + " sendTime : " + chatMessage.getSendTime());
                                        else if (chatMessage.getNature() == 5)
                                            System.out.println("\t\t\t\t\t\t图片:" + chatMessage.getImg() + " sendTime : " + chatMessage.getSendTime());
                                        else if (chatMessage.getNature() == 6)
                                            System.out.println("图片" + chatMessage.getImg() + " sendTime : " + chatMessage.getSendTime());
                                    }
                                    if (k == historyChatMessages.size() - 1)
                                        break;
                                    System.out.println("下一页(next);退出(quit)");
                                    resp = scanner.next();
                                } while (!resp.equals("quit"));
                            }
                        } else if (message.equals("Img")) {
                            /**发送图片**/
                            System.out.println("输入您要发送的图片全路径(包括文件名及其后缀)");
                            String img_path = scanner.next();
                            String result15 = ChatWrapper.sendImg(userID, anotherID, img_path);
                            if (result15.equals("success"))
                                System.out.println("图片发送成功!");
                            else if (result15.equals("error"))
                                System.out.println("图片发送失败...");
                            else if (result15.equals("false"))
                                System.out.println("图片不存在!");
                            else
                                System.out.println("出错?");
                        } else if (!message.equals("Submit") && !message.equals("Receive") && !message.equals("OnlineTransmit")) {
                            /**发送消息**/
                            String sendResult=ChatWrapper.sendMessage(userID,anotherID,message);
                            if(sendResult.equals("try"))
                                System.out.println("外网发送中");
                            else if (sendResult.equals("success"))
                                System.out.println("离线式发送成功!");
                            else if (sendResult.equals("false"))
                                System.out.println("发送失败...");
                            else
                                System.out.println("未知错误...");
                        } else if (message.equals("Submit")) {
                            /**发送文件**/

                            /**
                             * 离线发送
                             * **/
                            try {
                                System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀):");
                                String fileName = scanner.nextLine();

                                //发送文件的封装类方法
                                FileWrapper.sendFile(userID,anotherID,fileName,fileProgresses);

                            } catch (Exception e) {
                                System.out.println("离线文件传送失败...");
                                e.printStackTrace();
                            }
                        } else if (message.equals("Receive")) {
                                                /**接收/下载 (对方发送的)离线文件**/
                            System.out.println("\n请输入您要接收的离线文件名(包括后缀):");
                            String fileName = scanner.nextLine();
                            //存放目录酌情考虑
                            System.out.println("\n请输入您要存到的地方(即本地目录):");
                            String localPath = scanner.nextLine();

                            //接收文件的封装类方法
                            FileWrapper.receiveFile(anotherID,userID,localPath,fileName,fileProgresses);

                        } else if (message.equals("OnlineTransmit")) {
                            /**
                             * Transmit online file
                             * **/
                            System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                            String fileName = scanner.nextLine();

                            //在线发送文件的封装类方法
                            FileWrapper.onlineTransFile(userID,anotherID,fileName,fileMessages,fileProgresses);

                        }
                    }
                } else if (nextCommand.equals("Groups")) {
                    System.out.println("请输入您想要进入的群ID");
                    String groupID = scanner.nextLine();
                    SimpleGroup simpleGroup = groups.get(groupID);
                    String groupName = simpleGroup.getGroupName();
                    byte[] groupIcon = simpleGroup.getGroupIcon();                          /**null值**/
                    String groupIcon_Trans = new Gson().toJson(groupIcon);                  /**字符串的"null"**/
                    String theLatestMessage = simpleGroup.getTheLatestMessage();
                    String theLatestSendTime = simpleGroup.getTheLatestSendTime();
                    Map<String, String> parameters31 = new HashMap<String, String>();
                    parameters31.put("URL_ADDRESS", URL_ADDRESS);
                    parameters31.put("groupID", groupID);
                    parameters31.put("groupName", groupName);
                    parameters31.put("groupIcon", groupIcon_Trans);
                    parameters31.put("theLatestMessage", theLatestMessage);
                    parameters31.put("theLatestSendTime", theLatestSendTime);
                    Request request31 = new Request(URL_ADDRESS + "/GetFullGroup", parameters31, RequestProperty.APPLICATION);
                    String group_Trans = request31.doPost();

                    Gson gson31 = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type31 = new TypeToken<Group>() {
                    }.getType();
                    /**获得具有完整信息的群对象(但不包括群聊天记录)**/
                    Group group = gson31.fromJson(group_Trans, type31);
                    System.out.println("您进入到了该群中.");

                    /**开启监听本群成员信息变化的线程，并在此线程中处理信息的变化**/
                    ListenerAllMembersThread listenerAllMembersThread = new ListenerAllMembersThread(URL_ADDRESS, group);
                    Thread thread71 = new Thread(listenerAllMembersThread);
                    thread71.start();

                    /**获得该群聊天记录**/
                    Map<String, String> parameters32 = new HashMap<String, String>();
                    parameters32.put("groupID", groupID);
                    parameters32.put("exitTime", exitTime);
                    Request request32 = new Request(URL_ADDRESS + "/GetGroupChat", parameters32, RequestProperty.APPLICATION);
                    String result32 = request32.doPost();
                    Gson gson32 = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type32 = new TypeToken<ArrayList<GroupMessage>>() {
                    }.getType();
                    /**获得聊天信息列表并输出信息到控制台上(从上次退出时间开始的所有消息都在里面)**/
                    ArrayList<GroupMessage> groupMessageArrayList = gson32.fromJson(result32, type32);
                    for (GroupMessage groupMessage : groupMessageArrayList) {
                        /** 转换编码 **/
                        groupMessage.setContent(Chat.decodeChinese(groupMessage.getContent()));
                        /**PS:发送者头像是通过调用获取Group内部类指定ID的对象的方法，并调用此对象的getUserHeadIcon()方法获得的**/
                        if (groupMessage.getStatus() == 0) {
                            if (groupMessage.getContent() != null)
                                System.out.println("发送者头像:(无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + groupMessage.getSenderName() + " : " + groupMessage.getContent() + "(" + groupMessage.getSendTime() + ")");
                            else if (groupMessage.getImg() != null)
                                System.out.println("发送者头像:(无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + groupMessage.getSenderName() + " : " + "图片: " +/*groupMessage.getContent()*/"(无法显示)" + "(" + groupMessage.getSendTime() + ")");
                        } else if (groupMessage.getStatus() == 1) {
                            System.out.println("文件 From: " + groupMessage.getSenderName() + "发送者头像: (无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + "\tName: " + groupMessage.getContent());
                        }
                    }

                    System.out.println("\n聊天/Send\t发送图片/SendImg\t修改此群信息/Update\t退出该群/Quit\t退出群聊/ExitChat\tSendFile/上传文件");
                    String next;
                    while (true) {
                        next = null;
                        next = scanner.nextLine();
                        if (next.equals("Send")) {
                            /**聊天**/
                            System.out.println("输入您想说的:");
                            String content = scanner.nextLine();
                            String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
                            /**转换编码**/
                            content = Chat.encodeChinese(content);

                            Map<String, String> parameters33 = new HashMap<String, String>();
                            parameters33.put("groupID", groupID);
                            parameters33.put("senderID", userID);
                            parameters33.put("senderName", nickName);
                            parameters33.put("sendTime", sendTime);
                            parameters33.put("Status", "0");
                            parameters33.put("Content", content);

                            Request request33 = new Request(URL_ADDRESS + "/GroupSend", parameters33, RequestProperty.APPLICATION);
                            String result33 = request33.doPost();
                            System.out.println("发送成功!");
                        } else if (next.equals("SendImg")) {
                            /**发送群聊图片**/
                            System.out.println("输入您要发送的图片全路径(包括文件名及其后缀)");
                            String img_path = scanner.next();
                            java.io.File Img = new java.io.File(img_path);
                            InputStream inputStream = new FileInputStream(Img);
                            System.out.println("确认发送?(Y/N)");
                            String ifAgree = scanner.next();
                            while (!ifAgree.equals("Y") && !ifAgree.equals("N")) {
                                System.out.println("输入格式错误，请重新输入!");
                                ifAgree = scanner.next();
                            }
                            if (ifAgree.equals("Y")) {
                                //发送图片
                                byte[] ImgBytes = new byte[inputStream.available()];
                                inputStream.read(ImgBytes, 0, inputStream.available());
                                Gson gson = new Gson();
                                String ImgByteTrans = gson.toJson(ImgBytes);
                                String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
                                Map<String, String> parameters15 = new HashMap<String, String>();
                                parameters15.put("senderID", userID);
                                parameters15.put("senderName", user.getNickName());
                                parameters15.put("sendTime", sendTime);
                                parameters15.put("groupID", groupID);
                                parameters15.put("ImgBytes", ImgByteTrans);
                                Request request15 = new Request(URL_ADDRESS + "/SendGroupImg", parameters15, RequestProperty.APPLICATION);
                                String result15 = request15.doPost();
                                if (result15.equals("success"))
                                    System.out.println("图片发送成功!");

                                /*else if (result15.equals("error"))
                                    System.out.println("图片发送失败...");
                                else
                                    System.out.println("出错?");*/
                            } else {
                                continue;
                            }
                        } else if (next.equals("Update")) {
                            /**修改此群信息**/
                            System.out.println("\n检测您的身份...");
                            boolean isCreator = tools.Group.isCreator(group.getGroupMember(userID), groupID);
                            if (isCreator) {
                                System.out.println("您能够修改群消息...\n修改界面：");
                                System.out.println("旧的群昵称: " + group.getGroupName() +
                                        "\n旧的群头像: (无法显示)" +/*group.getGroupIcon()+*/
                                        "\n旧的群介绍: " + group.getGroupIntro()
                                );
                                System.out.println("新的群昵称: ");
                                String newGroupName = scanner.nextLine();

                                System.out.println("选择新头像路径：");
                                String path = scanner.nextLine();
                                java.io.File file = new java.io.File(path);
                                InputStream inputStream = null;
                                inputStream = new FileInputStream(file);
                                byte[] bytes = null;
                                if (inputStream != null) {
                                    bytes = new byte[inputStream.available()];
                                    inputStream.read(bytes, 0, inputStream.available());
                                    inputStream.close();
                                }

                                System.out.println("新的群介绍: ");
                                String newGroupIntro = scanner.nextLine();

                                group.setGroupName(newGroupName);
                                group.setGroupIcon(bytes);
                                group.setGroupIntro(newGroupIntro);

                                /**提交修改后的群信息资料**/
                                Map<String, String> parameters40 = new HashMap<String, String>();
                                Gson gson40 = new Gson();
                                String groupTrans = gson40.toJson(group);
                                parameters40.put("groupTrans", groupTrans);
                                Request request40 = new Request(URL_ADDRESS + "/ChangeGroupInfo", parameters40, RequestProperty.APPLICATION);
                                String result40 = request40.doPost();
                                System.out.println(result40);
                            } else {
                                System.out.println("您没有修改的权限...");
                            }
                        } else if (next.equals("Quit")) {
                            /**退出该群**/
                            Map<String, String> parameters35 = new HashMap<String, String>();
                            parameters35.put("userID", userID);
                            parameters35.put("groupID", groupID);
                            Request request35 = new Request(URL_ADDRESS + "/QuitGroup", parameters35, RequestProperty.APPLICATION);
                            String result35 = request35.doPost();

                            /**将全局变量群列表中的相应群删除**/
                            groups.remove(groupID);
                            /**将判断键值对中该群ID对应的值改为false**/
                            ifbreak.replace(groupID, false);

                            /**再加载一次登录时读取群列表的操作**/
                            /**实际程序应该用不到这步**/
                            ThreadWrapper.GroupListThread groupListThread = new ThreadWrapper.GroupListThread(userID);
                            Thread thread7 = new Thread(groupListThread);
                            thread7.start();
                        } else if (next.equals("ExitChat"))
                            break;
                        else if (next.equals("SendFile")) {
                                        /**发送文件**/
                            try {
                                System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                                String fileName = scanner.nextLine();
                                if (File.isChinese(fileName)) {
                                    System.out.println("不能有中文!");
                                    continue;
                                }
                                System.out.println("文件上传中,请等待成功提示(您可以退出此窗口,但不要退出程序...)");
                                GroupUploadThread groupUploadThread = new GroupUploadThread(userID, user.getNickName(), fileName, groupID);
                                Thread thread1 = new Thread(groupUploadThread);
                                thread1.start();
                            } catch (Exception e) {
                                System.out.println("离线文件传送失败...");
                                e.printStackTrace();
                            }
                        }
                    }
                    /**获得群聊信息和群成员列表**/
                } else if (nextCommand.equals("Add")) {
                    /**添加好友**/
                    System.out.println("--------------------------------------------------------------添加好友界面--------------------------------------------------------------");
                    Map<String,Contact> userList=OnlineWrapper.showAllFriends(userID);

                    if (userList != null && userList.size() > 0) {
                        System.out.println();
                        System.out.println("可添加列表:");
                        for (String ID : userList.keySet()) {
                            Contact contact = userList.get(ID);
                            //String thenickName = contact.getNickName();
                            Boolean isMale = contact.isMale();
                            String male;
                            if (isMale == null)
                                male = "未设定";
                            else if (isMale)
                                male = "男";
                            else
                                male = "女";
                            System.out.println("帐号:" + ID +
                                    "\t昵称:" + contact.getNickName() +
                                    "\t性别:" + male +
                                    "\t头像:" + /*contact.getHeadIcon()*/"无法显示" +
                                    "\t个人介绍/个性签名:" + contact.getIntro() +
                                    "\t是否在线:" + contact.isStatus()
                            );
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
                        String content1=OnlineWrapper.sendAddRequest(userID,receiverID);

                        if (content1.equals("CG"))
                            System.out.println("请求已发出，请等待对方的回复!");
                        else if (content1.equals("CF"))
                            System.out.println("您已发送过该邀请，请不要重复发送!");
                        else
                            throw new Exception("发送邀请出错!LoginClient");
                    }
                } else if (nextCommand.equals("AddGroup")) {
                    /**添加群**/
                    ArrayList<SimpleGroup> groupList=OnlineWrapper.showAllGroups(userID);
                    /**获得Group列表**/
                    for (SimpleGroup group : groupList) {
                        System.out.println(
                                "群ID: " + group.getGroupID() +
                                        "\t群昵称: " + group.getGroupName() +
                                        "\t群头像: " +/*group.getGroupIcon()*/"无法显示"
                        );
                    }

                    System.out.println("\n输入您想要加入的群:");
                    /**获得想要加入的群ID**/
                    String groupID = scanner.nextLine();
                    Gson gson30 = new Gson();
                    String headIconTrans = gson30.toJson(headIcon);
                    GroupWrapper.joinGroup(userID,groupID,nickName,headIconTrans,exitTime,groupMessages);

//                    /**将新获得的群的SimpleGroup对象添加到groups全局变量中**/
//                    groups.put(simpleGroup.getGroupID(), simpleGroup);


                    /**再加载一次登录时读取群列表的操作**//*
                    *//**实际程序应该用不到这步**//*
                    ThreadWrapper.GroupListThread groupListThread = new ThreadWrapper.GroupListThread(userID);
                    Thread thread8 = new Thread(groupListThread);
                    thread8.start();*/
                } else if (nextCommand.equals("Exit")) {
                    /**退出程序的过程**/
                    Map<String, String> parameter = new HashMap<String, String>();
                    parameter.put("userID", userID);
                    Request request2 = new Request(URL_ADDRESS + "/Exit", parameter, RequestProperty.APPLICATION);
                    String content = request2.doPost();                                    //content:error/exit两种可能
                    if (content.equals("exit")) {
                        System.out.println("exit...");
                        System.exit(1);
                    } else if (content.equals("error"))
                        System.out.println("退出异常!");
                } else if (nextCommand.equals("Submit")) {
                    /**上传离线文件**/
                    try {
                        System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀)");
                        String fileName = scanner.nextLine();

                        UploadSelfThread uploadSelfThread = new UploadSelfThread(userID, fileName, fileSocketAddress, fileds);
                        Thread thread52 = new Thread(uploadSelfThread);
                        thread52.start();
                        System.out.println("文件上传中,请等待成功提示(您可以退出此窗口,但不要退出程序...");


                        /*UploadFileRequest uploadFileRequest = new UploadFileRequest(fileName);                  //指定文件
                        String response = uploadFileRequest.upLoadFile(userID);                                 //指定用户userID
                        System.out.println(response);*/
                    } catch (Exception e) {
                        System.out.println("离线文件上传失败...");
                        e.printStackTrace();
                    }
                } else if (nextCommand.equals("CreateGroup")) {
                    /**创建群聊**/
                    Gson gson = new Gson();
                    String headIcon_Trans = gson.toJson(headIcon);                    //headIcon_Transy有可能等于null类型
                    System.out.println("输入群名称:");
                    String groupName = scanner.nextLine();
                    String createTime = String.valueOf(new DateTime().getCurrentDateTime());
                    Map<String, String> parameters20 = new HashMap<String, String>();
                    parameters20.put("groupName", groupName);
                    parameters20.put("creatorID", userID);
                    parameters20.put("headIcon", headIcon_Trans);
                    parameters20.put("createTime", createTime);
                    parameters20.put("creatorName", nickName);
                    Request request20 = new Request(URL_ADDRESS + "/CreateNewGroup", parameters20, RequestProperty.APPLICATION);
                    /**servlet返回新创建的群的ID**/
                    String result20 = request20.doPost();
                    String groupID = result20;
                    if (!request20.equals("error!CreateGroupServlet in Group.createNewGroup")) {
                        System.out.println("创建群成功!群ID: " + groupID);
                    }
                    /**添加监听此群消息的线程**/
                    ThreadWrapper.GroupListenerThread groupListenerThread = new ThreadWrapper.GroupListenerThread(userID, groupID, exitTime,groupMessages);
                    Thread thread20 = new Thread(groupListenerThread);
                    thread20.start();

                    /**再加载一次登录时读取群列表的操作**/
                    /**实际程序应该用不到这步**/
                    ThreadWrapper.GroupListThread groupListThread = new ThreadWrapper.GroupListThread(userID);
                    Thread thread7 = new Thread(groupListThread);
                    thread7.start();
                } else if (nextCommand.equals("UpdateInfo")) {
                               /**更改个人信息**/

                        /**显示个人信息**/
                        System.out.println("\n您当前的个人信息:\n");
                        System.out.println("\n帐号:" + user.getUserID() +
                                "\n密码: " + user.getPassWord() +
                                "\n昵称:" + user.getNickName() +
                                "\n性别:" + (user.isMale() == true ? "男" : "女") +
                                "\n生日:" + user.getBirthday() +
                                "\n手机号:" + user.getPhoneNum() +
                                "\n电子邮箱:" + user.getEmail() +
                                "\n头像:" +/*user.getHeadIcon()*/"无法呈现" +
                                "\n个人介绍" + (user.getIntro() == null ? "暂无" : user.getIntro()) +
                                "\n最后登录时间" + user.getExitTime());

                        /** 更新用户信息 **/
                        for (int i = 1; i < user.getPropertyNum(); i++) {
                            switch (i) {
                                case 1: {
                                    System.out.println("旧密码:" + user.getPassWord() + "\t输入新密码: ");
                                    user.setPassWord(scanner.nextLine());
                                    break;
                                }
                                case 2: {
                                    System.out.println("旧昵称:" + user.getNickName() + "\t输入新昵称: ");
                                    user.setNickName(scanner.nextLine());
                                    break;
                                }
                                case 3: {
                                    System.out.println("旧性别:" + (user.isMale() == true ? "男" : "女") + "\t输入新性别(男输入Y，女输入N):");
                                    user.setMale(((scanner.next().equals("Y")) ? true : false));
                                    break;
                                }
                                case 4: {
                                    System.out.println("旧生日:" + user.getBirthday() + "\t新生日(必须按照格式填写!格式: 2017-12-06 23:41:55 ):");
                                    String ifisbirthday = null;
                                    do {
                                        if (ifisbirthday != null)
                                            System.out.println("格式输入错误!请重新输入!");
                                        ifisbirthday = scanner.nextLine();
                                    } while (!ifisbirthday.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d"));
                                    user.setBirthday(ifisbirthday);
                                    System.out.println("成功更新用户生日! " + ifisbirthday);
                                    break;
                                }
                                case 5: {
                                    System.out.println("旧手机号: " + user.getPhoneNum() + "\t新手机号: ");
                                    user.setPhoneNum(scanner.next());
                                    break;
                                }
                                case 6: {
                                    System.out.println("旧电子邮箱: " + user.getEmail() + "\t新电子邮箱: ");
                                    String newEmail = scanner.nextLine();
                                    if (newEmail == null || newEmail.equals("")) {
                                        newEmail = scanner.nextLine();
                                    }
                                    user.setEmail(/*scanner.nextLine()*/newEmail);
                                    break;
                                }
                                case 7: {
                                    System.out.print("当前头像: " + "暂不显示\t新头像: ");
                                    System.out.println("更新头像？(Y/N)");
                                    String ifupdate = scanner.next();
                                    if (ifupdate.equals("Y")) {
                                        System.out.println("选择新头像路径：");
                                        String path = scanner.nextLine();
                                        while (path == null || path.equals("")) {
                                            System.out.println("选择新头像路径：");
                                            path = scanner.nextLine();
                                        }
                                        java.io.File file = new java.io.File(path);
                                        InputStream inputStream = new FileInputStream(file);
                                        byte[] bytes;
                                        if (inputStream != null) {
                                            bytes = new byte[inputStream.available()];
                                            inputStream.read(bytes, 0, inputStream.available());
                                            user.setHeadIcon(bytes);
                                        }
                                    } else {
                                        break;
                                    }
                                    break;
                                }
                                case 8: {
                                    System.out.println("旧个人介绍: " + user.getIntro() + "\t新个人介绍: ");
                                    user.setIntro(scanner.nextLine());
                                    break;
                                }
                                case 9: {
                                    System.out.println("上次下线时间: " + user.getExitTime());
                                    break;
                                }
                            }
                        }

                        String intro=null;
                        User user1=new User(userID,nickName,headIcon,intro);
                        OnlineWrapper.changeUserInfo(user);
                        /**提交修改**/
                        Gson gson = new Gson();
                        String commitChange = gson.toJson(user);
                        Map<String, String> parameters18 = new HashMap<String, String>();
                        parameters18.put("user", commitChange);
                        Request request18 = new Request(URL_ADDRESS + "/CommitChange", parameters18, RequestProperty.APPLICATION);
                        String result18 = request18.doPost();
                        System.out.println(result18);
                        System.out.println("修改个人信息过程结束!");

                }


                scanner.nextLine();
            }
        }
    }


    /**
     * ------------------------------------------------------主方法分割线--------------------------------------------------------
     **/




    //发送文件的线程
    private static class UploadThread implements Runnable {
        private String fileName;
        private String userID;
        private String anotherID;

        public UploadThread(String fileName, String userID, String anotherID) {
            this.fileName = fileName;
            this.userID = userID;
            this.anotherID = anotherID;
        }

        @Override
        public void run() {
            UploadFileRequest uploadFileRequest = new UploadFileRequest(fileName);                  //指定文件
            String response = null;                                 //指定用户userID
            try {
                response = uploadFileRequest.upLoadFile(userID, anotherID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (response.equals("success"))
                System.out.println("离线文件发送成功!");
        }
    }






    //发送群文件的线程
    private static class GroupUploadThread implements Runnable {
        private String userID;
        private String userName;
        private String fileName;
        private String groupID;

        public GroupUploadThread(String userID, String userName, String fileName, String groupID) {
            this.userID = userID;
            this.userName = userName;
            this.fileName = fileName;
            this.groupID = groupID;
        }

        @Override
        public void run() {
            GroupUploadFileRequest groupUploadFileRequest = new GroupUploadFileRequest(fileName);                  //指定文件
            String response = null;                                 //指定用户userID
            try {
                response = groupUploadFileRequest.upLoadFile(groupID, userID, userName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (response.equals("success"))
                System.out.println("离线文件发送成功!");
        }
    }

    //监听群成员头像等信息的变化
    private static class ListenerAllMembersThread implements Runnable {
        private String URL_ADDRESS;
        private Group group;
        private ArrayList<GroupMember> groupMembers;
        private Request request;
        private Map<String, String> parameters = new HashMap<String, String>();
        private String result;

        public ListenerAllMembersThread(String URL_ADDRESS, Group group) {
            this.URL_ADDRESS = URL_ADDRESS;
            this.group = group;
            this.parameters.put("groupID", this.group.getGroupID());
            this.request = new Request(this.URL_ADDRESS + "/ListenAllMembers", this.parameters, RequestProperty.APPLICATION);
            ifout.put(group.getGroupID(), true);
        }

        @Override
        public void run() {
            while (true) {
                if (!ifout.get(group.getGroupID())) {
                    //若退出了此界面，则停止监听
                    break;
                } else {
                    /**在此界面时，持续间断监听**/
                    result = null;
                    result = this.request.doPost();
                    if (result.equals("none")) {

                    } else {
                        groupMembers = null;
                        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                        Type type = new TypeToken<ArrayList<GroupMember>>() {
                        }.getType();

                        /**获得信息发生改变的GroupMember对象列表**/
                        groupMembers = gson.fromJson(result, type);

                        for (GroupMember groupMember : groupMembers) {
                            /**拿更改后的对象去更新列表中原有的**/
//                            group.getGroupMember(groupMember)
                            group.replaceGroupMemver(groupMember);
                        }

                        /**重画界面?**/
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

    //上传文件到个人文件夹的线程
    private static class UploadSelfThread implements Runnable {
        private String userID;
        private String fileName;
        private SocketAddress fileSocketAddress;
        private DatagramSocket fileds;

        public UploadSelfThread(String userID, String fileName, SocketAddress fileSocketAddress, DatagramSocket fileds) {
            this.userID = userID;
            this.fileName = fileName;
            this.fileSocketAddress = fileSocketAddress;
            this.fileds = fileds;
        }

        @Override
        public void run() {
            java.io.File file = new java.io.File(fileName);
            //System.out.println("file:\t"+file.length());
            if (file.exists()) {
                //如果文件存在（即不报错的话）
                byte[] buf = new byte[20 * 1024];
                byte[] receiveBuf = new byte[1];
                int readSize = -1;
                RandomAccessFile randomAccessFile = null;
                DatagramPacket dpk = null;
                try {
                    randomAccessFile = new RandomAccessFile(file, "r");
                    System.out.println("ranfile:\t" + randomAccessFile.length());
                    long sendCount = 0;
                    MyFileInfo messages;
                    String thefileName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
                    String info;
                    Gson gson51 = new Gson();
                    byte[] sendBytes;
                    messages = new MyFileInfo(userID, thefileName, buf);
                    while ((readSize = randomAccessFile.read(buf, 0, buf.length)) != -1) {
                        info = null;
                        messages.setFilebytes(buf);
                        info = gson51.toJson(messages);
                        sendBytes = ("MyTransFile/" + info).getBytes();
                        dpk = new DatagramPacket(sendBytes, 0, sendBytes.length, fileSocketAddress);
                        fileds.send(dpk);
                        sendCount++;
                        if (sendCount % 100 == 0 || sendCount == 1) {
                            System.out.println("Current: " + (sendCount * readSize) + " /" + file.length() + " (" + ((sendCount * readSize * 100) / file.length()) + "%)");
                        }
                        {
                            while (true) {
                                dpk.setData(receiveBuf, 0, receiveBuf.length);
                                fileds.receive(dpk);

                                // confirm server get
                                if (!UDPUtils.isEqualsByteArray(UDPUtils.successData, receiveBuf, dpk.getLength())) {
                                    System.out.println("resend ...");
                                    dpk.setData(sendBytes, 0, sendBytes.length);
                                    fileds.send(dpk);
                                } else
                                    break;
                            }
                        }
                    }
                    Map<String, String> parameters51 = new HashMap<String, String>();
                    parameters51.put("userID", userID);
                    parameters51.put("fileName", fileName);
                    Request request51 = new Request(URL_ADDRESS + "/MyUpload", parameters51, RequestProperty.APPLICATION);
                    String result51 = request51.doPost();
                    System.out.println(result51);
                    System.out.println("上传完成!");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }
}