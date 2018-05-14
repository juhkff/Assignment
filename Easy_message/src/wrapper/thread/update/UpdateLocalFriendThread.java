package wrapper.thread.update;

import wrapper.StaticVariable;

import java.io.*;
import java.net.Socket;

public class UpdateLocalFriendThread implements Runnable{
    private Socket socket;
    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private FileOutputStream fileOutputStream;

    public UpdateLocalFriendThread(Socket socket, InputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        try {
            this.fileOutputStream=new FileOutputStream(StaticVariable.USERINFO_FRIENDS_LIST);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        dataInputStream=new DataInputStream(inputStream);
        byte[] bytes=new byte[512];
        int len=0;
        try {
            while ((len = dataInputStream.read(bytes)) > 0) {
                fileOutputStream.write(bytes,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
                if (inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
