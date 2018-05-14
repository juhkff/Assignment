package wrapper.test;

import model.message.ChatMessage;
import tools.Chat;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import static wrapper.StaticVariable.*;

public class TestBlockingThread implements Runnable{
    private BlockingQueue<ChatMessage> chatMessages;

    public TestBlockingThread(BlockingQueue<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public void run() {
        while (true) {
            ChatMessage chatMessage = null;
            try {
                chatMessage = chatMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //                chatMessage.setMessage(Chat.decodeChinese(chatMessage.getMessage()));
            //            System.out.println("有新消息!");
            String senderID;
            String senderName;
            String content = null;

            byte[] senderHeadIcon = null;                    /**发送方的头像不是从ChatMessage类中获得的，而是从联系人列表中摘取的**/

            byte nature;
            byte[] imgBytes = null;
            String sendTime;

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
    }
}
