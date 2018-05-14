package wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.message.ChatMessage;
import test.Client.Request;
import test.Client.RequestProperty;
import tools.Chat;
import tools.DateTime;

import java.io.*;
import java.lang.reflect.Type;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static wrapper.StaticVariable.*;
/*import static test.Client.LoginClient.Thread_Index;
import static test.Client.LoginClient.URL_ADDRESS;*/

public class ChatWrapper {



    //1、
    /**
     *
     * @param userID
     * @param anotherID
     * @return ArrayList<ChatMessage> historyChatMessages (size=0 or size>0)
     */
    //获得与指定用户的所有聊天记录
    public static ArrayList<ChatMessage> getSimpleChat(String userID, String anotherID) {
        Map<String, String> parameters1 = new HashMap<String, String>();
        parameters1.put("userID", userID);
        parameters1.put("anotherID", anotherID);
        Request request1 = new Request(URL_ADDRESS + "/getChatHistory", parameters1, RequestProperty.APPLICATION);
        String chatHistoryList = request1.doPost();
        if (chatHistoryList.equals("null")) {
            ArrayList<ChatMessage> historyChatMessages = new ArrayList<ChatMessage>();
            return historyChatMessages;
        } else {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<ArrayList<ChatMessage>>() {
            }.getType();
            ArrayList<ChatMessage> historyChatMessages = gson.fromJson(chatHistoryList, type);                  //historyChatMessages存储聊天记录

            return historyChatMessages;
        }
    }



    //2、
    /**
     *
     * @param userID
     * @param anotherID
     * @param img_path
     * @return "success" || "error" || "false"
     * @throws IOException
     */
    //发送图片
    public static String sendImg(String userID, String anotherID, String img_path) throws IOException {
        String result15=null;
        java.io.File Img = new java.io.File(img_path);
        if (!Img.exists()) {
            result15 = "false";
        } else {
            InputStream inputStream = new FileInputStream(Img);
            byte[] ImgBytes = new byte[inputStream.available()];
            inputStream.read(ImgBytes, 0, inputStream.available());
            String ImgByteTrans = GsonTrans.bytestoString(ImgBytes);
            String sendTime = String.valueOf(new DateTime().getCurrentDateTime());
            Map<String, String> parameters15 = new HashMap<String, String>();
            parameters15.put("userID", userID);
            parameters15.put("anotherID", anotherID);
            parameters15.put("FileBytes", ImgByteTrans);
            parameters15.put("sendTime", sendTime);
            Request request15 = new Request(URL_ADDRESS + "/SendImg", parameters15, RequestProperty.APPLICATION);
            result15 = request15.doPost();
        }
        return result15;
         /*
        if(result15.equals("success"))
            System.out.println("图片发送成功!");
        else if(result15.equals("error"))
            System.out.println("图片发送失败...");
        else if(result15.equals("false"))
            System.out.println("找不到文件");
        */
    }



    //3、
    /**
     *
     * @param userID
     * @param anotherID
     * @param message
     * @return "try"(when online) || "success"(when offline)
     * @throws UnsupportedEncodingException
     */
    //发送消息
    public static String sendMessage(String userID,String anotherID,String message/*, DatagramSocket messageds, SocketAddress messageSocketAddress*/)
            throws UnsupportedEncodingException {
        byte nature=0;
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

            ThreadWrapper.SendThread sendThread = new ThreadWrapper.SendThread(Thread_Index,userID, anotherID, nature, sendTime, message);
            ifis.put(Thread_Index++, false);
            Thread thread12 = new Thread(sendThread);
            thread12.start();
            return "try";


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
            String result3 = request3.doPost();               //result3 : "success" || "false"
            return result3;
        }
    }
}
