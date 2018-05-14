package wrapper;

import model.file.FileProgress;
import model.message.FileMessage;
import wrapper.thread.OnlineTransThread;
import wrapper.thread.ReceiveFileThread;
import wrapper.thread.TransFileThread;

import java.util.concurrent.BlockingQueue;

public class FileWrapper {
    public static void sendFile(String senderID, String receiverID, String fileName, BlockingQueue<FileProgress> fileProgresses){
        TransFileThread transFileThread = new TransFileThread(/*userID*/senderID, /*anotherID*/receiverID, fileName,fileProgresses);
        Thread thread51 = new Thread(transFileThread);
        thread51.start();
        System.out.println("文件离线发送中,请等待成功提示(您可以退出此窗口,但不要退出程序...");
    }

    public static void receiveFile(String senderID,String receiverID,String localPath,String fileName,BlockingQueue<FileProgress> fileProgresses){
        System.out.println("开始接收,请等待接收完毕的提醒(您可以退出此窗口,但不要退出程序...)");

        ReceiveFileThread receiveFileThread = new ReceiveFileThread(/*anotherID*/senderID, /*userID*/receiverID, localPath, fileName,fileProgresses);

        Thread thread1 = new Thread(receiveFileThread);
        thread1.start();
    }

    public static void onlineTransFile(String senderID, String receiverID, String fileName, BlockingQueue<FileMessage> fileMessages,BlockingQueue<FileProgress> fileProgresses){

        //在线直传文件的总程序
        OnlineTransThread onlineTransThread=new OnlineTransThread(/*userID*/senderID,/*anotherID*/receiverID,fileName,fileMessages,fileProgresses);
        Thread thread=new Thread(onlineTransThread);
        thread.start();
    }
}
