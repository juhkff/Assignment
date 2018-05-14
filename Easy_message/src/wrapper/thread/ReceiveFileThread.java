package wrapper.thread;

import model.file.FileProgress;
import test.Client.DownloadFileRequest;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

//接收离线文件的线程
public class ReceiveFileThread implements Runnable {
    private String anotherID = null;
    private String userID = null;
    private String localPath;
    private String fileName;
    private String groupID = null;
    private BlockingQueue<FileProgress> fileProgresses;

    public ReceiveFileThread(String anotherID, String userID, String localPath, String fileName, BlockingQueue<FileProgress> fileProgresses) {
        this.anotherID = anotherID;
        this.userID = userID;
        this.localPath = localPath;
        this.fileName = fileName;
        this.fileProgresses=fileProgresses;
    }

    public ReceiveFileThread(String groupID, String localPath, String fileName,BlockingQueue<FileProgress> fileProgresses) {
        this.groupID = groupID;
        this.localPath = localPath;
        this.fileName = fileName;
        this.fileProgresses=fileProgresses;
    }

    @Override
    public void run() {
        if (anotherID != null) {
            Socket client = null;
            File file = null;
            long currentSize = 0;
            long fileLength = 0;
            try {
                client = new Socket("123.207.13.112", 4444);
                OutputStream out = client.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
//                dos.writeUTF("ReceiveAnother");
                dos.writeUTF(anotherID);
                dos.writeUTF(userID);
                dos.writeUTF(fileName);                                                                                 //文件名
                String receivePath=localPath;                                                                           //本地要存储文件的父目录
                file=new File(receivePath);
                if(!file.exists()) {
                    file.mkdirs();
                }

                FileOutputStream fileOutputStream=new FileOutputStream(receivePath+File.separator+fileName);
                InputStream inputStream=client.getInputStream();
                DataInputStream dataInputStream=new DataInputStream(inputStream);
                fileLength=dataInputStream.readLong();
                System.out.println("文件大小:"+(fileLength/1024)+"KB");
                byte[] buffer = new byte[1024];
                int len = 0;
                double percent = 0; // 文件下载比例
                currentSize = 0; // 当前文件下载大小

                FileProgress fileProgress;
                while ((len = dataInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                    currentSize += len;
                    percent = (double) currentSize / fileLength;
                    int currentPer = (int) (percent * 100);
//                    System.out.println("已下载：" + (currentSize/1024) + "KB" + "\t下载进度：" + currentPer + "%");
                    fileProgress=new FileProgress(currentSize,fileLength,currentPer);
                    fileProgresses.put(fileProgress);                                                                   //加入到进度条队列中
                }
                inputStream.close();
                out.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                client.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }finally {
                if (currentSize!=fileLength){
                    if (file.exists())
                        file.delete();
                }
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