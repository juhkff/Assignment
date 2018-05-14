package tools.file.model;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import model.file.FileProgress;
import tools.file.File;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class AcceptThread implements Runnable{
    private Socket socket;
    private BlockingQueue<FileProgress> fileProgresses;
    private String fileDir;
    private String fileName;
    private String filePath;
    private InputStream inputStream;
    private FileOutputStream fileOutputStream;
    private long fileLength;

    public AcceptThread(Socket socket, BlockingQueue<FileProgress> fileProgresses,String filePath) {
        this.socket = socket;
        this.fileProgresses = fileProgresses;
        this.filePath=filePath;
    }

    @Override
    public void run() {
        java.io.File file=new java.io.File(filePath);
        if (!file.exists()){
            file.mkdirs();                                                                                              //如果父目录不存在，则创建父目录
        }
        inputStream=null;
        fileOutputStream=null;
        long currentSize=0;
        try {
            inputStream=socket.getInputStream();
            DataInputStream dataInputStream=new DataInputStream(inputStream);
            fileName=dataInputStream.readUTF();
            fileLength=dataInputStream.readLong();
            fileOutputStream=new FileOutputStream(filePath+ java.io.File.separator+fileName);
            byte[] bytes=new byte[1024];
            double percent;
            int currentPercent=0;
            int len=0;
            while ((len=dataInputStream.read(bytes))>0){
                fileOutputStream.write(bytes,0,len);
                currentSize+=len;
                percent=(double)currentSize/fileLength;
                currentPercent=(int)(percent*100);
                FileProgress fileProgress=new FileProgress(currentSize,fileLength,currentPercent);
                fileProgresses.put(fileProgress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                socket.close();
                if (currentSize!=fileLength){
                    file=new java.io.File(filePath+ java.io.File.separator+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
