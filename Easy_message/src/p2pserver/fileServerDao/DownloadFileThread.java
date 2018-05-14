package p2pserver.fileServerDao;

import model.file.FileProgress;
import tools.file.File;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class DownloadFileThread implements Runnable{
    private Socket socket;
    private String senderID;
    private String receiverID;
    private String fileName;
    private String filePath;

    public DownloadFileThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        FileInputStream fileInputStream=null;
        InputStream inputStream=null;
        OutputStream outputStream=null;

        try {
            inputStream=socket.getInputStream();
            DataInputStream dataInputStream=new DataInputStream(inputStream);
            outputStream=socket.getOutputStream();
            senderID=dataInputStream.readUTF();
            receiverID=dataInputStream.readUTF();
            fileName=dataInputStream.readUTF();
            filePath="C:\\Easy_message\\Upload\\"+senderID+"_To_"+receiverID+"\\"+fileName;
            java.io.File file=new java.io.File(filePath);
            fileInputStream=new FileInputStream(file);
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
            long fileLength=file.length();
            dataOutputStream.writeLong(fileLength);
            int len=0;
            long currentSize=0;
            double percent=0;
            byte[] bytes=new byte[2048];

            while ((len=fileInputStream.read(bytes))>0){
                currentSize+=len;
                percent=(double)currentSize/fileLength;
                int currentPer=(int)(percent*100);
                dataOutputStream.write(bytes,0,len);
                System.out.println("已下载:"+(currentSize)+"Bytes"+"\t下载进度:"+currentPer+"%");
            }
            outputStream.close();
            fileInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
