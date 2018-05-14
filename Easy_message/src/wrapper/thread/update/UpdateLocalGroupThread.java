package wrapper.thread.update;

import wrapper.StaticVariable;

import java.io.*;
import java.net.Socket;

public class UpdateLocalGroupThread implements Runnable{
    private Socket socket;
    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private FileOutputStream fileOutputStream;

    public UpdateLocalGroupThread(Socket socket, InputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            fileOutputStream=new FileOutputStream(StaticVariable.USERINFO_GROUPS_LIST);
            dataInputStream=new DataInputStream(inputStream);
            byte[] bytes=new byte[512];
            int len=0;
            while ((len = dataInputStream.read(bytes)) > 0) {
                fileOutputStream.write(bytes,0,len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
