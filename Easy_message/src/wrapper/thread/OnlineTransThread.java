package wrapper.thread;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.file.FileProgress;
import model.message.FileMessage;
import p2pserver.fileServerDao.TransferThread;
import test.Client.LoginClient;
import test.Client.Request;
import test.Client.RequestProperty;
import tools.DateTime;
import tools.file.model.FileSenderThread;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.*;

public class OnlineTransThread implements Runnable{
    private String userID;
    private String anotherID;
    private String fileName;
    private BlockingQueue<FileMessage> fileMessages;
    private BlockingQueue<FileProgress> fileProgresses;

    public OnlineTransThread(String userID, String anotherID, String fileName,BlockingQueue<FileMessage> fileMessages,BlockingQueue<FileProgress> fileProgresses) {
        this.userID = userID;
        this.anotherID = anotherID;
        this.fileName = fileName;
        this.fileMessages=fileMessages;
        this.fileProgresses=fileProgresses;
    }

    @Override
    public void run() {
        /**尝试局域网发送**/
        Map<String, String> parameters2 = new HashMap<String, String>();
        parameters2.put("senderID", userID);
        parameters2.put("anotherID", anotherID);
        Request request2 = new Request(/*LoginClient.*/URL_ADDRESS + "/GetLocalAddress", parameters2, RequestProperty.APPLICATION);
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
            TryOnlineTransitThread tryOnlineTransitThread = new TryOnlineTransitThread(Thread_Index, userID, anotherID, address, addressList);
            int cur_index = Thread_Index;
            Thread thread14 = new Thread(tryOnlineTransitThread);
            ifis.put(Thread_Index++, false);
            senderAddr = null;
            receiverAddr = null;
            System.out.println("尝试局域网发送...");
            thread14.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                    TransFileThread uploadThread = new TransFileThread(userID, anotherID,fileName,fileProgresses);
                    Thread thread1 = new Thread(uploadThread);
                    thread1.start();
                }
            }
        }


        /**----------------------------------------------------------------------------**/
        if (senderAddr != null && receiverAddr != null) {

            java.io.File file = new java.io.File(fileName);

            /**获得文件输入流后改名以便于发送和接收**/
            String fileNameChanged = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
            String fileSize = String.valueOf(file.length());
            System.out.println("等待对方同意接收...");
            FileMessage fileMessage = new FileMessage(userID, contacts.get(userID).getNickName(), anotherID, contacts.get(anotherID).getNickName(), fileNameChanged, fileSize);
            Gson gson2 = new GsonBuilder().enableComplexMapKeySerialization().create();
            String fileMes = gson2.toJson(fileMessage);

            messagesendby = ("SendFile/" + fileMes).getBytes();
            messagedp = new DatagramPacket(messagesendby, 0, messagesendby.length, messageSocketAddress);
            try {
                messageds.send(messagedp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Map<String, String> parameters3 = new HashMap<String, String>();
            parameters3.put("userID", userID);
            parameters3.put("anotherID", anotherID);
            parameters3.put("message", fileNameChanged);
            String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
            parameters3.put("sendTime", sendTime);
            //插入数据库的代码
            Request request3 = new Request(URL_ADDRESS + "/sendFileRequest", parameters3, RequestProperty.APPLICATION);
            String result3 = request3.doPost();
            if (result3.equals("success")) {
                /**通过一方向数据库表中插入此条发送文件的信息，另一方更新数据库表中修改此条文件的接收
                 * 情况，发送方检测此条文件的发送情况来实现双方同意的情况下开始文件传输**/
                ReadFileResponseThread readFileResponse = new ReadFileResponseThread(userID, anotherID, fileName, fileNameChanged,receiverAddr.split(",")[0],fileProgresses);
                Thread thread9 = new Thread(readFileResponse);
                thread9.start();
            } else {
                System.out.println("请求发送失败...");
            }
        }
    }


    //在线发送消息的线程
    private static class TryOnlineTransitThread implements Runnable {
        private int index;
        private String senderID;
        private String anotherID;
        private String address;
        private String addressList;
        private String message;
        private SocketAddress socketAddress;
        private DatagramPacket dp = null;
        private byte[] bytes = null;

        public TryOnlineTransitThread(int index, String senderID, String anotherID, String address, String addressList) {
            this.index = index;
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
                messageds.send(dp);
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
        private String receiverIP;
        private Request request;
        private Map<String, String> parameters;
        private BlockingQueue<FileProgress> fileProgresses;

        public ReadFileResponseThread(String userID, String anotherID, String fileName, String fileNameChanged,String receiverIP,BlockingQueue<FileProgress> fileProgresses) {
            this.userID = userID;
            this.anotherID = anotherID;
            this.fileName = fileName;
            this.fileNameChanged = fileNameChanged;
            this.receiverIP=receiverIP;
            this.fileProgresses=fileProgresses;
            parameters = new HashMap<String, String>();
            parameters.put("userID", userID);
            parameters.put("anotherID", anotherID);
            parameters.put("fileName", fileNameChanged);
            request = new Request(URL_ADDRESS + "/ReadFileResponse", parameters, RequestProperty.APPLICATION);
        }

        @Override
        public void run() {
            while (true) {
                String isAccepted = request.doPost();             //每隔3s请求一次servlet,获得返回值
                if (!isAccepted.equals("N")) {
                    if (isAccepted.equals("T")) {
                        /**这里写局域网用udp传输文件的方法**/
                        FileSenderThread fileSenderThread = new FileSenderThread(fileName, this.receiverIP,fileProgresses);
                        Thread thread=new Thread(fileSenderThread);
                        thread.start();
                        break;
                    } else if (isAccepted.equals("F")) {
                        System.out.println("对方拒绝接收文件" + fileNameChanged + "...");
                        break;
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
}
