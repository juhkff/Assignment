package server.bridgeThread;

import server.model.ServerCommand;
import wrapper.StaticVariable;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static server.staticvariable.ServerStatic.socketMap;

public class DownloadGroupJsonThread implements Runnable{
    private BlockingQueue<ServerCommand> groupJsonCommand;

    public DownloadGroupJsonThread(BlockingQueue<ServerCommand> groupJsonCommand) {
        this.groupJsonCommand = groupJsonCommand;
    }

    @Override
    public void run() {
        while (true){
            try {
                ServerCommand  serverCommand = groupJsonCommand.take();
                String userID=serverCommand.getUserID();
                Socket socket=socketMap.get(userID);
                OutputStream outputStream=socket.getOutputStream();
                DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                dataOutputStream.writeUTF("Groups");
                File file=new File(StaticVariable.getUserinfoGroupsList(userID));
                //向客户端发送文件的过程
                DownloadFileThread downloadFileThread=new DownloadFileThread(socket,outputStream,file);
                Thread thread=new Thread(downloadFileThread);
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
