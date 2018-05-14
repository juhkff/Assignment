package wrapper.thread;

import model.file.FileProgress;
import test.Client.Request;
import test.Client.RequestProperty;
import tools.DateTime;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.FILE_SERVER_IP;
import static wrapper.StaticVariable.FILE_SERVER_SOCKET_PORT;
import static wrapper.StaticVariable.URL_ADDRESS;

//离线向好友传输文件的线程
public final class TransFileThread implements Runnable {
    private String userID;
    private String anotherID=null;
    private String fileName;
    private BlockingQueue<FileProgress> fileProgresses;
    public TransFileThread(String userID, String anotherID, String fileName, BlockingQueue<FileProgress> fileProgresses) {
        this.userID = userID;
        this.anotherID = anotherID;
        this.fileName = fileName;
        this.fileProgresses=fileProgresses;
    }

    @Override
    public void run() {
        java.io.File file = new java.io.File(fileName);
        String Name=file.getName();
        long fileLength=file.length();
        if (file.exists()) {
            Socket client=null;
            try {
                client=new Socket(FILE_SERVER_IP,FILE_SERVER_SOCKET_PORT);
                OutputStream outputStream=client.getOutputStream();
                DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                dataOutputStream.writeUTF("Transfer");
                dataOutputStream.writeUTF(Name);                                                                    /*文件名*/
                dataOutputStream.writeUTF(userID);
                dataOutputStream.writeUTF(anotherID);
                dataOutputStream.writeLong(fileLength);

                FileInputStream fileInputStream=new FileInputStream(file);
                byte[] bytes=new byte[2048];
                int len=0;
                double percent=0;                                                                                   //文件上传比例
                long size=0;                                                                                        //文件上传目前大小

                while ((len=fileInputStream.read(bytes))>0){
                    size+=len;
                    percent=(double)size/fileLength;                                                                //获得目前上传进度
                    int currentPer=(int)(percent*100);                                                              //强转为int
                    dataOutputStream.write(bytes,0,len);
//                    System.out.println("已上传：" + (size / 1024) + "KB" + "\t上传比例：" + currentPer + "%");
                    FileProgress fileProgress=new FileProgress(size,fileLength,currentPer);
                    fileProgresses.put(fileProgress);                                                               //加入到BlockingQueue队列中
                }
                //关闭客户端
                outputStream.close();
                fileInputStream.close();
                client.close();


                String sendTime= String.valueOf(new DateTime().getCurrentDateTime());
                Map<String,String> parameters=new HashMap<String, String>();
                parameters.put("userID",userID);
                parameters.put("anotherID",anotherID);
                parameters.put("fileName",fileName);
//                parameters.put("sendTime",sendTime);
                Request request=new Request(URL_ADDRESS+"/Upload",parameters,RequestProperty.APPLICATION);
                String result=request.doPost();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}