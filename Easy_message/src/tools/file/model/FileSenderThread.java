package tools.file.model;

import model.file.FileProgress;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;

public class FileSenderThread implements Runnable{
    private final String SEND_FILE_PATH;
    private String receiverIP;
    private int receiverPORT=5555;
    private File file;
    private BlockingQueue<FileProgress> fileProgresses;

    public FileSenderThread(String SEND_FILE_PATH,String receiverIP,BlockingQueue<FileProgress> fileProgresses) {
        this.SEND_FILE_PATH = SEND_FILE_PATH;
        this.file=new File(this.SEND_FILE_PATH);
        this.receiverIP=receiverIP;
        this.fileProgresses=fileProgresses;
    }

    @Override
    public void run() {
        OutputStream outputStream=null;
        FileInputStream fileInputStream=null;
        Socket socket=null;
        long fileLength;
        long currentSize = 0;
        try {
            socket=new Socket(receiverIP,receiverPORT);
            outputStream=socket.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
            File file=new File(SEND_FILE_PATH);
            fileLength=file.length();
            String fileName=file.getName();
            dataOutputStream.writeUTF(fileName);                                                                        //文件名
            dataOutputStream.writeLong(fileLength);                                                                     //文件大小
            fileInputStream=new FileInputStream(file);
            byte[] bytes=new byte[2048];
            double percent;
            int len=0;
            int currentPer;                                                                                             //发送进度百分比数值
//            String current;                                                                                             //发送进度百分比
            FileProgress fileProgress;
            System.out.println("对方同意接收文件!文件传输开始...");
            while ((len=fileInputStream.read(bytes))>0){
                currentSize+=len;
                percent=(double)currentSize/fileLength;
                currentPer=(int)(percent*100);
                dataOutputStream.write(bytes,0,len);
//                current=currentPer+"%";
                fileProgress=new FileProgress(currentSize,fileLength,currentPer);
                fileProgresses.put(fileProgress);                                                                       //把进度放到队列中
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileInputStream!=null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
