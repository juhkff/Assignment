package wrapper.test;

import model.message.NoticeMessage;
import wrapper.OnlineWrapper;
import wrapper.ThreadWrapper;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.URL_ADDRESS;

public class TestNoticeMessageThread implements Runnable{
    private BlockingQueue<NoticeMessage> noticeMessages;
    private String userID;

    public TestNoticeMessageThread(String userID,BlockingQueue<NoticeMessage> noticeMessages) {
        this.noticeMessages = noticeMessages;
        this.userID=userID;
    }

    @Override
    public void run() {
        while (true){
            NoticeMessage noticeMessage = null;
            try {
                noticeMessage=noticeMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            String anotherID = noticeMessage.getAnotherID();
            String nickName = noticeMessage.getNickName();
            byte property = noticeMessage.getProperty();
            if (property == 0) {                                             //好友邀请
                System.out.println("\n来自帐号:" + anotherID + "  昵称为" + nickName + " 的用户的好友邀请");
                String response1 = "";
                System.out.println("\n同意还是拒绝? (Y/N)");
                response1 = new Scanner(System.in).next();
                if (response1.equals("Y")) {
                    /**用户同意添加好友**/
                    String result1 = OnlineWrapper.agreeAdd(userID, noticeMessage);
                    if (result1.equals("success")) {
                        System.out.println("添加成功!");
                    }
           /*         *//**重新开启一个获取联系人的线程**//*
                    ThreadWrapper.ContactListThread contactListThread = new ThreadWrapper.ContactListThread(userID, URL_ADDRESS + "/ContactList");
                    Thread thread6 = new Thread(contactListThread);
                    thread6.start();
                    thread6.join();*/
                }else {
//                    continue;
                }
            }else if (property == 1)
                System.out.println("1");
            else if (property == 2) {
                System.out.println("\n添加好友:" + anotherID + "  昵称:" + nickName + "  成功");
            } else if (property == 3)
                System.out.println("3");

        }
    }
}
