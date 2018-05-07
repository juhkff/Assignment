package test.Client;

import com.fasterxml.jackson.databind.type.ArrayType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.contact.Contact;
import model.group.Group;
import model.group.GroupMessage;
import model.group.SimpleGroup;
import model.message.ChatMessage;
import model.message.FileMessage;
import model.message.NoticeMessage;
import model.property.User;
import tools.Chat;
import tools.DateTime;
import tools.file.File;
import tools.file.model.FileReceiver;
import tools.file.model.FileSender;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LoginClient {
    /**
     * 设置全局变量
     **/
    public static final String URL_ADDRESS = "http://123.207.13.112:8080/Easy_message";

    //将线程中获得的ArrayList<NoticeMessage>存储为全局变量
    public static ArrayList<NoticeMessage> noticeMessages;
    /**
     * 好友列表
     **/
    public static Map<String, Contact> contacts;
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

    /**
     * 小细节：这里要是不用Map<String,Contact>类型而用<ArrayList>类型的话，在根据ID查找用户上就会很吃效率(大概)
     **/

    //public static DatagramSocket messageds = null;
    //public static DatagramSocket fileds = null;
    public static void main(String[] args) throws Exception {
        String userID = "2461247724";               //juhkff
        //String userID = "8133523681";               //juhkgf
        String passWord = "aqko251068";

        /**登录工作过程**/
        String nickName;
        byte[] headIcon;
        String exitTime;

        /**与udp相关的基础工作**/
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
//        /**向servlet发送请求验证帐号和密码的步骤没有写**/
        //现在写了
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userID", userID);
        parameters.put("passWord", passWord);
        Request request = new Request(URL_ADDRESS + "/Login", parameters, RequestProperty.APPLICATION);
        String result = request.doPost();     //获得验证结果
        User user = null;
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
                user = gson.fromJson(userInfo, type);
                /*加上密码*/
                user.setPassWord(passWord);

                /**输出用户基本信息**/
                System.out.println("用户的数据(已经转换为了User类对象):" + userInfo);
                /**获取用户的nickName属性**/
                nickName = user.getNickName();
                headIcon = user.getHeadIcon();
                exitTime = user.getExitTime();
            }


            //除非登录时向两个服务器发送的数据包都得到了应答(回复数据包)，否则重复发送
            String messageresult;
            String fileresult;

            //获取客户端上线时的局域网地址
            /**暴力获取**/
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
            ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(messageds, fileds, messageSocketAddress, URL_ADDRESS, userID);
            Thread thread = new Thread(receiveMessageThread);
            thread.start();

           /* //建立接收与文件有关的线程
            FileListenerThread fileListenerThread=new FileListenerThread(fileds,fileSocketAddress);
            Thread thread10=new Thread(fileListenerThread);
            thread10.start();*/

            //创建所有线程
            startAllThread(userID, messageds, fileds, messageSocketAddress, fileSocketAddress, exitTime);

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
                                String ImgByteTrans = gson.toJson(ImgBytes);
                                String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
                                Map<String, String> parameters15 = new HashMap<String, String>();
                                parameters15.put("userID", userID);
                                parameters15.put("anotherID", anotherID);
                                parameters15.put("FileBytes", ImgByteTrans);
                                parameters15.put("sendTime", sendTime);
                                Request request15 = new Request(URL_ADDRESS + "/SendImg", parameters15, RequestProperty.APPLICATION);
                                String result15 = request15.doPost();
                                if (result15.equals("success"))
                                    System.out.println("图片发送成功!");
                                else if (result15.equals("error"))
                                    System.out.println("图片发送失败...");
                                else
                                    System.out.println("出错?");
                            } else {
                                continue;
                            }
                        } else if (!message.equals("Submit") && !message.equals("Receive") && !message.equals("OnlineTransmit")) {
                            /**发送消息**/
                            if (message.contains("'")) {
                                String[] temp = message.split("'");
                                message = "";
                                for (int j = 0; j < temp.length - 1; j++) {
                                    message += (temp[j] + "\\'");
                                }
                                message += temp[temp.length - 1];
                            }
                            if (message.contains("\"")) {
                                String[] temp = message.split("\"");
                                message = "";
                                for (int j = 0; j < temp.length - 1; j++) {
                                    message += (temp[j] + "\\\"");
                                }
                                message += temp[temp.length - 1];
                            }
                            boolean isOnline = Chat.checkOnlineStatus(anotherID);
                            message = Chat.encodeChinese(message);
                            if (isOnline) {
                                System.out.println("在线，尝试外网发送...");
                                //若是对方在线
                                /**
                                 * 优先用外网发送
                                 * **/
                                String sendTime = String.valueOf(new DateTime().getCurrentDateTime());

                                SendThread sendThread = new SendThread(Thread_Index, messageds, messageSocketAddress, userID, anotherID, nature, sendTime, message);
                                ifis.put(Thread_Index++, false);
                                Thread thread12 = new Thread(sendThread);
                                thread12.start();


                                /**
                                 * Chat.insertChatMessage(userID,anotherID,message,Timestamp.valueOf(sendTime));
                                 * Chat.updateContactStatus(userID,anotherID);
                                 * **/
                            } else {
                                String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
                                Map<String, String> parameters3 = new HashMap<String, String>();
                                parameters3.put("userID", userID);
                                parameters3.put("anotherID", anotherID);
                                parameters3.put("message", message);
                                parameters3.put("sendTime", sendTime);
                                Request request3 = new Request(URL_ADDRESS + "/OfflineChat", parameters3, RequestProperty.APPLICATION);
                                String result3 = request3.doPost();               //result2 : success / false;
                                System.out.println(result3);
                            }
                        } else if (message.equals("Submit")) {
                            /**发送文件**/

                            /**
                             * 离线发送
                             * **/
                            try {
                                System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                                String fileName = scanner.nextLine();
                                if (File.isChinese(fileName)) {
                                    System.out.println("不能有中文!");
                                    continue;
                                }
                                System.out.println("文件上传中,请等待成功提示(您可以退出此窗口,但不要退出程序...)");
                                UploadThread uploadThread = new UploadThread(fileName, userID, anotherID);
                                Thread thread1 = new Thread(uploadThread);
                                thread1.start();
                                /*UploadFileRequest uploadFileRequest = new UploadFileRequest(fileName);                  //指定文件
                                String response = uploadFileRequest.upLoadFile(userID,anotherID);                                 //指定用户userID
                                System.out.println(response);*/
                            } catch (Exception e) {
                                System.out.println("离线文件传送失败...");
                                e.printStackTrace();
                            }
                        } else if (message.equals("Receive")) {
                            /**接收/下载 (对方发送的)离线文件**/
                            System.out.println("\n请输入您要接收的离线文件名(包括后缀):");
                            String fileName = scanner.nextLine();
                            System.out.println("\n请输入您要存到的地方(即本地目录):");
                            String localPath = scanner.nextLine();
                            System.out.println("开始接收,请等待接收完毕的提醒(您可以退出此窗口,但不要退出程序...)");
                            ReceiveFileThread receiveFileThread = new ReceiveFileThread(anotherID, userID, localPath, fileName);
                            Thread thread1 = new Thread(receiveFileThread);
                            thread1.start();
                            /*DownloadFileRequest downloadFileRequest=new DownloadFileRequest(anotherID,userID,localPath,fileName);
                            downloadFileRequest.downLoad();*/
                        } else if (message.equals("OnlineTransmit")) {
                            /**
                             * Transmit online file
                             * **/
                            System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                            String fileName = scanner.nextLine();

                            /**尝试局域网发送**/
                            Map<String, String> parameters2 = new HashMap<String, String>();
                            parameters2.put("senderID", userID);
                            parameters2.put("anotherID", anotherID);
                            Request request2 = new Request(LoginClient.URL_ADDRESS + "/GetLocalAddress", parameters2, RequestProperty.APPLICATION);
                            String addressList = request2.doPost();
                            String addresses = addressList.split("/")[1];
                            addressList = addressList.split("/")[0];
                            Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization().create();
                            Type type1 = new TypeToken<ArrayList<String>>() {
                            }.getType();
                            ArrayList<String> addrList = new ArrayList<String>();
                            addrList = gson1.fromJson(addresses, type1);
                            //ArrayList<String> anotherAddrList=gson1.fromJson(addressList,type1);
                            String senderAddr = null;
                            String receiverAddr = null;
                            for (int index = 0; index < addrList.size(); index++) {
                                String address = addrList.get(index);
                                TryOnlineTransitThread tryOnlineTransitThread = new TryOnlineTransitThread(Thread_Index, messageds, userID, anotherID, address, addressList);
                                int cur_index = Thread_Index;
                                Thread thread14 = new Thread(tryOnlineTransitThread);
                                ifis.put(Thread_Index++, false);
                                senderAddr = null;
                                receiverAddr = null;
                                System.out.println("尝试局域网发送...");
                                thread14.start();
                                Thread.sleep(2000);
                                if (ifis.get(cur_index)) {                       /**true**/
                                    //可以局域网内连通
                                    senderAddr = senderFileAddress.get(cur_index);                            //发送方的地址
                                    receiverAddr = receiverFileAddress.get(cur_index);                        //接收方的地址
                                    break;
                                } else {
                                    if (index < addrList.size() - 1)
                                        continue;
                                    else {
                                        System.out.println("局域网发送无效，尝试数据库离线式发送...");
                                        System.out.println("文件上传中,请等待成功提示(您可以退出此窗口,但不要退出程序...)");
                                        UploadThread uploadThread = new UploadThread(fileName, userID, anotherID);
                                        Thread thread1 = new Thread(uploadThread);
                                        thread1.start();
                                    }
                                }
                            }


                            /**实现起来有点找不到头绪,就随便写写了...**/
                            ///**
                            // * 这里写用udp传输文件的方法,------>ds用fileds!!!
                            // * **/
                            /**----------------------------------------------------------------------------**/
                            if (senderAddr != null && receiverAddr != null) {
                                /**senderAddr和receiverAddr是消息地址**/
                                /**获得文件地址**/
                                Map<String, String> parameters5 = new HashMap<String, String>();
                                parameters5.put("senderAddr", senderAddr);
                                parameters5.put("receiverAddr", receiverAddr);
                                Request request5 = new Request(URL_ADDRESS + "/getFileAddress", parameters5, RequestProperty.APPLICATION);
                                String result5 = request5.doPost();//获得文件地址
                                senderAddr = result5.split("/")[0];
                                receiverAddr = result5.split("/")[1];

                                java.io.File file = new java.io.File(fileName);
                               /* double Size = Double.parseDouble(new DecimalFormat("#.00").format(((double) file.length()) / (1024 * 1024)));    //获得文件大小(默认MB)
                                String fileSize;
                                if (Size >= 1024) {
                                    Size /= 1024;
                                    fileSize = Size + "GB";
                                } else {
                                    fileSize = Size + "MB";
                                }*/
                                /**获得文件输入流后改名以便于发送和接收**/
                                String fileNameChanged = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
                                String fileSize = String.valueOf(file.length());
                                System.out.println("等待对方同意接收...");
                                FileMessage fileMessage = new FileMessage(userID, contacts.get(userID).getNickName(), anotherID, contacts.get(anotherID).getNickName(), senderAddr, receiverAddr, fileNameChanged, fileSize);
                                Gson gson2 = new GsonBuilder().enableComplexMapKeySerialization().create();
                                String fileMes = gson2.toJson(fileMessage);

                                messagesendby = ("SendFile/" + fileMes).getBytes();
                                messagedp = new DatagramPacket(messagesendby, 0, messagesendby.length, messageSocketAddress);
                                messageds.send(messagedp);

                                Map<String, String> parameters3 = new HashMap<String, String>();
                                parameters3.put("userID", userID);
                                parameters3.put("anotherID", anotherID);
                                parameters3.put("message", fileNameChanged);
                                String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
                                parameters3.put("sendTime", sendTime);
                                Request request3 = new Request(URL_ADDRESS + "/sendFileRequest", parameters3, RequestProperty.APPLICATION);
                                String result3 = request3.doPost();
                                if (result3.equals("success")) {
                                    /**通过一方向数据库表中插入此条发送文件的信息，另一方更新数据库表中修改此条文件的接收
                                     * 情况，发送方检测此条文件的发送情况来实现双方同意的情况下开始文件传输**/
                                    ReadFileResponseThread readFileResponse = new ReadFileResponseThread(userID, anotherID, fileName,fileNameChanged ,URL_ADDRESS, senderAddr, receiverAddr, fileds);
                                    Thread thread9 = new Thread(readFileResponse);
                                    thread9.start();
                                } else {
                                    System.out.println("请求发送失败...");
                                }
                            }
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
                    ArrayList<GroupMessage> groupMessages = gson32.fromJson(result32, type32);
                    for (GroupMessage groupMessage : groupMessages) {
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
                            }else {
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
                            GroupListThread groupListThread = new GroupListThread(userID, URL_ADDRESS);
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
                    Map<String, String> parameter = new HashMap<String, String>();
                    parameter.put("userID", userID);
                    Request request1 = new Request(URL_ADDRESS + "/AddContact", parameter, RequestProperty.APPLICATION);
                    String content = request1.doPost();
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<Map<String, Contact>>() {
                    }.getType();
                    //Map<userID,nickName>
                    Map<String, Contact> userList = gson.fromJson(content, type);
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
                } else if (nextCommand.equals("AddGroup")) {
                    /**添加群**/
                    Map<String, String> parameters21 = new HashMap<String, String>();
                    parameters21.put("userID", userID);
                    Request request21 = new Request(URL_ADDRESS + "/AddGroup", parameters21, RequestProperty.APPLICATION);
                    String groupListTrans = request21.doPost();
                    Gson gson21 = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type21 = new TypeToken<ArrayList<SimpleGroup>>() {
                    }.getType();
                    ArrayList<SimpleGroup> groupList = gson21.fromJson(groupListTrans, type21);
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
                    Map<String, String> parameters30 = new HashMap<String, String>();
                    parameters30.put("URL_ADDRESS", URL_ADDRESS);
                    parameters30.put("userID", userID);
                    parameters30.put("groupID", groupID);
                    parameters30.put("userName", nickName);
                    parameters30.put("userHeadIconTrans", headIconTrans);
                    Request request30 = new Request(URL_ADDRESS + "/JoinGroup", parameters30, RequestProperty.APPLICATION);
                    /**客户端获得的返回值是SimpleGroup对象**/
                    String result30 = request30.doPost();
//                    System.out.println(result30);
                    Gson gson31=new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type31=new TypeToken<SimpleGroup>(){}.getType();
                    SimpleGroup simpleGroup=gson31.fromJson(result30,type31);
                    /**将新获得的群的SimpleGroup对象添加到groups全局变量中**/
                    groups.put(simpleGroup.getGroupID(),simpleGroup);

                    /**添加监听此群的线程**/
                    GroupListenerThread groupListenerThread = new GroupListenerThread(userID, groupID, exitTime, URL_ADDRESS);
                    Thread thread30 = new Thread(groupListenerThread);
                    thread30.start();

                    /**再加载一次登录时读取群列表的操作**/
                    /**实际程序应该用不到这步**/
                    GroupListThread groupListThread = new GroupListThread(userID, URL_ADDRESS);
                    Thread thread8 = new Thread(groupListThread);
                    thread8.start();
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
                } else if (nextCommand.equals("Deal")) {
                    /**处理好友请求**/
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
                        System.out.println("来自帐号 " + noticeMessage.getAnotherID() + "的昵称为" + noticeMessage.getNickName() + "的好友邀请? (Y)");
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
                                    if (result1.equals("success")) {
                                        System.out.println("添加成功!");

                                        /**重新开启一个获取联系人的线程**/
                                        ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList");
                                        Thread thread6 = new Thread(contactListThread);
                                        thread6.start();
                                        thread6.join();
                                    }
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
                } else if (nextCommand.equals("Submit")) {
                    /**上传离线文件**/
                    try {
                        System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀)");
                        String fileName = scanner.nextLine();

                        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileName);                  //指定文件
                        String response = uploadFileRequest.upLoadFile(userID);                                 //指定用户userID
                        System.out.println(response);
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
                    GroupListenerThread groupListenerThread = new GroupListenerThread(userID, groupID, exitTime, URL_ADDRESS);
                    Thread thread20 = new Thread(groupListenerThread);
                    thread20.start();

                    /**再加载一次登录时读取群列表的操作**/
                    /**实际程序应该用不到这步**/
                    GroupListThread groupListThread = new GroupListThread(userID, URL_ADDRESS);
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
                                String newEmail=scanner.nextLine();
                                if(newEmail==null||newEmail.equals("")){
                                    newEmail=scanner.nextLine();
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
                                    while (path==null||path.equals("")){
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
                    char thefirst = ip.charAt(0);
                    if (thefirst > 47 && thefirst < 58 && !ip.contains(":"))
                        allLocalMessageAddress.add(ip + "," + clientMessagePORT);
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
                    char thefirst = ip.charAt(0);
                    if (thefirst > 47 && thefirst < 58 && !ip.contains(":"))
                        allLocalFileAddress.add(ip + "," + clientFilePORT);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return allLocalFileAddress;

    }

    //开启所有线程的方法
    private static void startAllThread(String userID, DatagramSocket messageds, DatagramSocket fileds, SocketAddress messageSocketAddress, SocketAddress fileSocketAddress, String exitTime) throws SQLException, InterruptedException {


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

        /**创建获取好友列表的线程**/
        ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList");
        Thread thread6 = new Thread(contactListThread);
        thread6.start();
        thread6.join();                                                             /**这个join应该管用**/

        /**创建获取群列表的线程**/
        GroupListThread groupListThread = new GroupListThread(userID, URL_ADDRESS);
        Thread thread7 = new Thread(groupListThread);
        thread7.start();
        thread7.join();

        /**对每个群都建立一个监听群消息的线程**/
        for (int i = 0; i < groups.size(); i++) {
            Set<String> groupIDList = groups.keySet();
            Iterator<String> iterator = groupIDList.iterator();
            while (iterator.hasNext()) {
                String groupID = iterator.next();
                GroupListenerThread groupListenerThread = new GroupListenerThread(userID, groupID, exitTime, URL_ADDRESS);
                Thread thread = new Thread(groupListenerThread);
                thread.start();
            }
        }

        /**建立用户全群监听线程**/
        AllGroupListenerThread allGroupListenerThread=new AllGroupListenerThread(userID,URL_ADDRESS);
        Thread thread=new Thread(allGroupListenerThread);
        thread.start();

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
        /**创建获取好友列表的线程**//*
         *//**
         * 应该和后面的线程组放在一起?
         * **//*
        ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList");
        Thread thread6 = new Thread(contactListThread);
        thread6.start();*/

    }

    //接收消息的线程类
    private static class ReceiveMessageThread implements Runnable {
        private DatagramSocket ds;
        private DatagramSocket fileds;
        private SocketAddress messageSocketAddress;
        private DatagramPacket dp;
        private byte[] by;
        private String message;
        private String URL_ADDRESS;
        private String userID;

        public ReceiveMessageThread(DatagramSocket ds, DatagramSocket fileds, SocketAddress messageSocketAddress, String URL_ADRESS, String userID) {
            this.ds = ds;
            this.fileds = fileds;
            this.messageSocketAddress = messageSocketAddress;
            by = new byte[1024 * 8];
            this.URL_ADDRESS = URL_ADRESS;
            this.userID = userID;
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


                Gson gson1 = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type1 = new TypeToken<ArrayList<String>>() {
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
                    String content = message.split("/")[2];
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type = new TypeToken<ChatMessage>() {
                    }.getType();
                    ChatMessage chatMessage = gson.fromJson(content, type);
                    chatMessage.setNature((byte) 1);

//                    String callBackID = chatMessage.getAnotherID();
                    String callBackID = chatMessage.getSenderID();
                    //String myID = chatMessage.getAnotherID();
                    String myID = userID;
                    byte[] bytes = ("CallBack/" + message.split("/")[1] + "/" + callBackID + "/" + myID).getBytes();
                    /*Map<String, String> parameter9 = new HashMap<String, String>();
                    parameter9.put("userID", callBackID);
                    Request request = new Request(URL_ADDRESS + "/getMessageAddress", parameter9, RequestProperty.APPLICATION);
                    String result = request.doPost();
                    SocketAddress anotherMessageSocketAddress = new InetSocketAddress(result.split(":")[0], Integer.parseInt(result.split(":")[1]));
                    */
                    dp = new DatagramPacket(bytes, 0, bytes.length, messageSocketAddress/*anotherMessageSocketAddress*/);
                    try {
                        ds.send(dp);                                    //应该不用再测试是否能发送成功了
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String nickName = LoginClient.contacts.get(callBackID).getNickName();                 //可以从静态变量中取得发送用户的所有基本信息
                    String text = null;
                    try {
                        text = Chat.decodeChinese(chatMessage.getMessage());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String sendTime = chatMessage.getSendTime();

                    System.out.println("收到服务器转发的来自:" + callBackID + " 昵称为" + nickName + "的新消息:" + text + "\t发送时间:" + sendTime);
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
                    /*Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
                    Type type1=new TypeToken<ArrayList<String>>(){
                    }.getType();
                    ArrayList<String> addressList=gson.fromJson(callBackAddress)*/
                    for (String address : addresses) {
                        String ip = address.split(",")[0];
                        int port = Integer.parseInt(address.split(",")[1]);
                        SocketAddress socketAddress = new InetSocketAddress(ip, port);

                        byte[] bytes = ("CallLocalBack/" + callBackID + "/" + myID).getBytes();
                        dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
                        try {
                            ds.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    String nickName = LoginClient.contacts.get(callBackID).getNickName();
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
                            ds.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (message.startsWith("SendFile")) {
                    /**接收到在线发送文件的请求**/
                    ResponseThread responseThread = new ResponseThread(dp, fileds, URL_ADDRESS);
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
            private DatagramSocket ds;
            private String message;
            private FileMessage fileMessage;
            private String URL_ADDRESS;

            public ResponseThread(DatagramPacket dp, DatagramSocket ds, String URL_ADDRESS) {
                this.dp = dp;
                this.ds = ds;
                this.URL_ADDRESS = URL_ADDRESS;
                this.message = new String(dp.getData(), 0, dp.getLength());
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<FileMessage>() {
                }.getType();
                this.fileMessage = gson.fromJson(message.substring(9, message.length()), type);
            }

            @Override
            public void run() {
                System.out.println("\n在线发送请求:来自" + fileMessage.getSenderID() + "\t昵称" + fileMessage.getSenderNickName()
                        + "\t文件名" + fileMessage.getFileName() + "\t文件大小" + fileMessage.getFileSize() + "\n");
                System.out.println("\n是否同意接收?(Y/N)");
                Scanner scanner = new Scanner(System.in);
                String ifAgree = scanner.next();  /**同意或拒绝**/

                /**可以在任何时候接收或拒绝**/
                while (!ifAgree.equals("Y") && !ifAgree.equals("N")) {
                    System.out.println("输入格式错误...");
                    System.out.println("\n是否同意接收?(Y/N)");
                    ifAgree = scanner.next();  /**同意或拒绝**/
                }

                if (ifAgree.equals("Y")) {
                    //System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                    System.out.println("\n请输入您的存储路径(目标文件夹全路径):");
                    String save_path = scanner.next();
                    if(!save_path.endsWith("\\"))
                        save_path+=("\\"+fileMessage.getFileName());
                    else
                        save_path+=fileMessage.getFileName();
                    FileReceiver fileReceiver = new FileReceiver((Long.parseLong(fileMessage.getFileSize())), save_path, fileMessage.getSenderAddress(), fileMessage.getReceiverAddress(), ds);
                    /**暂停主接收?**/

                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("senderID", fileMessage.getSenderID());
                    parameters.put("fileName", fileMessage.getFileName());

                    System.out.println(fileMessage.getFileName());

                    Request request = new Request(URL_ADDRESS + "/overrideProcess", parameters, RequestProperty.APPLICATION);
                    String result = request.doPost();

                    fileReceiver.receive();
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


    //监听好友(非群)中的上下线等行为(通过定时访问数据库实现)
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
    private final static class GroupListThread implements Runnable {
        private Map<String, SimpleGroup> simpleGroupArrayList = new HashMap<String, SimpleGroup>();
        private String userID;
        private String URL_ADDRESS;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Request request;
        private String groupsTrans;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private Type type = new TypeToken<Map<String, SimpleGroup>>() {
        }.getType();

        public GroupListThread(String userID, String URL_ADDRESS) {
            this.userID = userID;
            this.URL_ADDRESS = URL_ADDRESS;
            parameters.put("userID", this.userID);
            this.request = new Request(this.URL_ADDRESS + "/GroupList", parameters, RequestProperty.APPLICATION);
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
                        else if (property == 2) {
                            System.out.println("\n添加好友:" + anotherID + "  昵称:" + nickName + "  成功");
                            /**重新开启一个获取联系人的线程**/
                            ContactListThread contactListThread = new ContactListThread(userID, URL_ADDRESS + "/ContactList");
                            Thread thread6 = new Thread(contactListThread);
                            thread6.start();
                            try {
                                thread6.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else if (property == 3)
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

    //获取聊天消息的线程
    private final static class ChatNoticeThread implements Runnable {
        private String userID;
        private Map<String, String> parameter = new HashMap<String, String>();
        private Request request;
        private String notices;
        private ArrayList<ChatMessage> chatMessages;

        public ChatNoticeThread(String userID) {
            this.userID = userID;
            this.parameter.put("userID", this.userID);
            this.request = new Request(URL_ADDRESS + "/GetChat", this.parameter, RequestProperty.APPLICATION);
        }

        @Override
        public void run() {
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
//            while (true) {
            this.notices = this.request.doPost();
            if (notices.equals("") || notices.equals("none")) {
                //System.out.println("\n您(离线时)未收到过任何消息!");
            } else {
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<ArrayList<ChatMessage>>() {
                }.getType();
                this.chatMessages = gson.fromJson(this.notices, type);
                /**解析Json**/
                String senderID;
                String senderName;
                String content = null;

                byte[] senderHeadIcon = null;                    /**发送方的头像不是从ChatMessage类中获得的，而是从联系人列表中摘取的**/

                byte nature;
                byte[] imgBytes = null;
                String sendTime;
                System.out.println("\n未读聊天消息:\n");
                for (ChatMessage chatMessage : chatMessages) {
                    senderHeadIcon = null;

                    senderID = chatMessage.getAnotherID();
                    senderName = contacts.get(senderID).getNickName();                    /**根据ID获得发送者的昵称**/

                    senderHeadIcon = contacts.get(senderID).getHeadIcon();                  /**根据ID获得发送者的头像**/

                    try {
                        content = chatMessage.getMessage();
                        imgBytes = chatMessage.getImg();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    sendTime = chatMessage.getSendTime();
                    nature = chatMessage.getNature();
                    if (content != null && nature == 1)
                        System.out.println("来自" + senderID + "  昵称为 " + senderName + "发送者头像为: (无法显示)" + " 的消息: " + content + " 发送时间: " + sendTime);
                    else if (imgBytes != null && nature == 5)
                        System.out.println("来自" + senderID + "  昵称为" + senderName + "发送者头像为: (无法显示)" + " 的图片: " + /*imgBytes*/"(无法显示)" + " 发送时间: " + sendTime);
                }
                //System.out.println("\n未读聊天消息显示完毕!\n");
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            }
        }
    }

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

    //接收离线文件的线程
    private static class ReceiveFileThread implements Runnable {
        private String anotherID = null;
        private String userID = null;
        private String localPath;
        private String fileName;
        private String groupID = null;

        public ReceiveFileThread(String anotherID, String userID, String localPath, String fileName) {
            this.anotherID = anotherID;
            this.userID = userID;
            this.localPath = localPath;
            this.fileName = fileName;
        }

        public ReceiveFileThread(String groupID, String localPath, String fileName) {
            this.groupID = groupID;
            this.localPath = localPath;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            if (anotherID != null) {
                DownloadFileRequest downloadFileRequest = new DownloadFileRequest(anotherID, userID, localPath, fileName);
                try {
                    downloadFileRequest.downLoad();
                    System.out.println("文件接收成功!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (this.groupID != null) {
                /**下载群文件**/
                DownloadFileRequest downloadFileRequest = new DownloadFileRequest(groupID, localPath, fileName);
                try {
                    downloadFileRequest.downLoad();
                    System.out.println("文件接收成功!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //在线发送消息的线程
    private static class TryOnlineTransitThread implements Runnable {
        private int index;
        private DatagramSocket ds;
        private String senderID;
        private String anotherID;
        private String address;
        private String addressList;
        private String message;
        private SocketAddress socketAddress;
        private DatagramPacket dp = null;
        private byte[] bytes = null;

        public TryOnlineTransitThread(int index, DatagramSocket ds, String senderID, String anotherID, String address, String addressList) {
            this.index = index;
            this.ds = ds;
            this.senderID = senderID;
            this.anotherID = anotherID;
            this.address = address;
            this.addressList = addressList;
            this.message = "OnlineTransmit/" + this.index + "/" + address + "/" + addressList;
        }

        @Override
        public void run() {
            String ip = address.split(",")[0];
            int port = Integer.parseInt(address.split(",")[1]);
            socketAddress = new InetSocketAddress(ip, port);

            bytes = this.message.getBytes();
            dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //发送在线文件后接收开始信号的线程
    private static class ReadFileResponseThread implements Runnable {
        private String userID;
        private String anotherID;
        private String fileName;
        private String fileNameChanged;
        private String URL_ADDRESS;
        private String senderAddr;
        private String receiverAddr;
        private DatagramSocket ds;

        private Request request;
        private Map<String, String> parameters;


        public ReadFileResponseThread(String userID, String anotherID, String fileName, String fileNameChanged, String URL_ADDRESS, String senderAddr, String receiverAddr, DatagramSocket ds) {
            this.userID = userID;
            this.anotherID = anotherID;
            this.fileName = fileName;
            this.fileNameChanged=fileNameChanged;
            this.URL_ADDRESS = URL_ADDRESS;
            this.senderAddr = senderAddr;
            this.receiverAddr = receiverAddr;
            this.ds = ds;
        }

        @Override
        public void run() {
            while (true) {
                parameters = new HashMap<String, String>();
                parameters.put("userID", userID);
                parameters.put("anotherID", anotherID);
                parameters.put("fileName", fileNameChanged);
                request = new Request(this.URL_ADDRESS + "/ReadFileResponse", parameters, RequestProperty.APPLICATION);
                String isAccepted = request.doPost();             //每隔3s请求一次servlet,获得返回值
                if (!isAccepted.equals("N")) {
                    if (isAccepted.equals("T")) {
                        /**这里写局域网用udp传输文件的方法**/
                        FileSender fileSender = new FileSender(fileName, this.senderAddr, this.receiverAddr, ds);
                        System.out.println("对方同意接收文件!文件传输开始...");
                        System.out.println(fileSender.send());
                        break;
                    } else if (isAccepted.equals("F")) {
                        System.out.println("对方拒绝接收文件" + fileNameChanged + "...");
                    }
                } else {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //发送消息的线程
    private static class SendThread implements Runnable {
        private int index;
        private DatagramSocket ds;
        private SocketAddress socketAddress;
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

        public SendThread(int index, DatagramSocket messageds, SocketAddress messageSocketAddress, String senderID, String anotherID, byte nature, String sendTime, String message) {
            this.index = index;
            this.ds = messageds;
            this.socketAddress = messageSocketAddress;
            this.senderID = senderID;
            this.anotherID = anotherID;
            this.nature = nature;
            this.sendTime = sendTime;
            this.message = message;
            this.chatMessage = new ChatMessage(senderID, anotherID, nature, sendTime, message);
        }

        @Override
        public void run() {
            String content = gson.toJson(chatMessage);
            bytes = ("Chat/" + this.index + "/" + content).getBytes();
            dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
            try {
                ds.send(dp);
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

    //(每个群)监听群消息更新的线程
    private static class GroupListenerThread implements Runnable {
        private String userID;
        private String groupID;
        private String referredTime;
        private String URL_ADDRESS;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Request request;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private Type type = new TypeToken<ArrayList<GroupMessage>>() {
        }.getType();
        private ArrayList<GroupMessage> groupMessages = null;

        public GroupListenerThread(String userID, String groupID, String exitTime, String URL_ADDRESS) {
            this.userID = userID;
            this.groupID = groupID;
            this.referredTime = exitTime;
            this.URL_ADDRESS = URL_ADDRESS;
            this.parameters.put("userID", userID);
            this.parameters.put("groupID", groupID);
            this.parameters.put("referredTime", referredTime);
            this.request = new Request(this.URL_ADDRESS + "/GroupListener", parameters, RequestProperty.APPLICATION);
            /**向全局变量ifbreak中添加此群的存在判断键值对**/
            ifbreak.put(this.groupID, true);
        }

        @Override
        public void run() {
            String result = null;
            while (true) {
                /**用户退出该群时，会将ifbreak中该群对应的值改为false，若检查为false，则退出此线程**/
                if (!ifbreak.get(this.groupID))
                    break;
                result = null;
                this.request = new Request(this.URL_ADDRESS + "/GroupListener", this.parameters, RequestProperty.APPLICATION);
                groupMessages = null;
                result = this.request.doPost();
                if (!result.equals("none")) {
                    /**获得消息列表**/
                    groupMessages = gson.fromJson(result, type);
                    System.out.println("\n收到新的群聊消息!");
                    for (GroupMessage groupMessage : groupMessages) {
                        try {
                            /**转换编码**/
                            groupMessage.setContent(Chat.decodeChinese(groupMessage.getContent()));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            System.out.println("error in LoginClient GroupListenerThread !");
                        }
                        /**PS:发送者头像是通过调用获取Group内部类指定ID的对象的方法，并调用此对象的getUserHeadIcon()方法获得的**/
                        if (groupMessage.getStatus() == 0) {
                            if (groupMessage.getContent() != null)
                                System.out.println("From: " + groupID + " 群昵称: " + groups.get(groupID).getGroupName() + "发送者头像:(无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + groupMessage.getSenderName() + " : " + groupMessage.getContent() + "(" + groupMessage.getSendTime() + ")");
                            else if (groupMessage.getImg() != null)
                                System.out.println("From: " + groupID + " 群昵称: " + groups.get(groupID).getGroupName() + "发送者头像:(无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + groupMessage.getSenderName() + " : " + "发送的图片:" +/*groupMessage.getImg()*/"(无法显示)" + "(" + groupMessage.getSendTime() + ")");
                        } else if (groupMessage.getStatus() == 1) {
                            System.out.println("From: " + groupID + " 群昵称: " + groups.get(groupID).getGroupName() + "文件 From: " + groupMessage.getSenderName() + "发送者头像: (无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + "\tName: " + groupMessage.getContent());
                        }
                    }
                    /**更改groups全局变量中的最新消息以及最新消息发送时间**/
                    groups.get(groupID).setTheLatestMessage(groupMessages.get(groupMessages.size() - 1).getContent());
                    groups.get(groupID).setTheLatestSendTime(groupMessages.get(groupMessages.size() - 1).getSendTime());

                    /**更换参考时间为接收到的消息的最新时间**/
                    parameters.replace("referredTime", groupMessages.get(groupMessages.size() - 1).getSendTime());

                    /**以下代码是为了让程序能够在控制台上显示出变化加的额外程序，实际中应该不需要**/
                    Map<String, String> parameters2 = new HashMap<String, String>();
                    parameters2.put("userID", userID);
                    parameters2.put("groupID", groupID);
                    Request request2 = new Request(this.URL_ADDRESS + "/UpdateGroupInfo", parameters2, RequestProperty.APPLICATION);
                    String result2 = request2.doPost();

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
    private static class AllGroupListenerThread implements Runnable {
        private String userID;
        private String URL_ADDRESS;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Request request;
        private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        private Type type = new TypeToken<Map<String, SimpleGroup>>() {
        }.getType();
        private String result;

        public AllGroupListenerThread(String userID, String URL_ADDRESS) {
            this.userID = userID;
            this.URL_ADDRESS = URL_ADDRESS;
            this.parameters.put("userID", userID);
            this.request = new Request(this.URL_ADDRESS + "/ListenAllGroups", parameters, RequestProperty.APPLICATION);
        }

        @Override
        public void run() {
            while (true) {
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
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
}