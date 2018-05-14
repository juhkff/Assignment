package wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

import static wrapper.StaticVariable.*;

public class SocketWrapper {



    //发送本地局域网地址给服务器存储

    public static void sendLocalAddress(String userID/*,DatagramSocket messageds,DatagramSocket fileds,String MESSAGE_SERVER_IP,
                                        int MESSAGE_SERVER_PORT, String FILE_SERVER_IP,int FILE_SERVER_PORT*/){
        //除非登录时向两个服务器发送的数据包都得到了应答(回复数据包)，否则重复发送
        String messageresult;
        String fileresult;

        byte[] messagerecby = new byte[1024 * 8];
        byte[] filerecby = new byte[1024 * 8];

        //获取客户端上线时的局域网地址
        /**暴力获取**/
        do {
            //获取客户端上线时的局域网地址
            /**暴力获取**/
            int clientMessagePORT = messageds.getLocalPort();
            int clientFilePORT = fileds.getLocalPort();
            ArrayList<String> messageAddress = getLocalMessageAddress(clientMessagePORT);
            ArrayList<String> fileAddress = getLocalFileAddress(clientFilePORT);

            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            String messageAddressList = gson.toJson(messageAddress);
            String fileAddressList = gson.toJson(fileAddress);

            byte[] messagesendby = ("Login/" + userID + "/" + messageAddressList).getBytes();
            byte[] filesendby = ("Login/" + userID + "/" + fileAddressList).getBytes();

            SocketAddress messageSocketAddress = new InetSocketAddress(MESSAGE_SERVER_IP, MESSAGE_SERVER_PORT);
            SocketAddress fileSocketAddress = new InetSocketAddress(FILE_SERVER_IP, FILE_SERVER_PORT);

            DatagramPacket messagedp = new DatagramPacket(messagesendby, 0, messagesendby.length, messageSocketAddress);
            DatagramPacket filedp = new DatagramPacket(filesendby, 0, filesendby.length, fileSocketAddress);

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


            messageresult = new String(messagedp.getData(), 0, messagedp.getLength());
            fileresult = new String(filedp.getData(), 0, messagedp.getLength());

        } while (!messageresult.equals("success") && !fileresult.equals("success"));
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

}
