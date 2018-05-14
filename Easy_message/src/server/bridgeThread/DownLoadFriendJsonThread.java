package server.bridgeThread;

import server.model.ServerCommand;
import wrapper.StaticVariable;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static server.staticvariable.ServerStatic.socketMap;

public class DownLoadFriendJsonThread implements Runnable{
    private BlockingQueue<ServerCommand> friendJsonCommand;

    public DownLoadFriendJsonThread(BlockingQueue<ServerCommand> friendJsonCommand) {
        this.friendJsonCommand = friendJsonCommand;
    }

    @Override
    public void run() {
        while (true){
            try {
                ServerCommand serverCommand=friendJsonCommand.take();
                String userID=serverCommand.getUserID();
                Socket socket=socketMap.get(userID);
                OutputStream outputStream=socket.getOutputStream();
                DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                dataOutputStream.writeUTF("Friends");
                File file=new File(StaticVariable.getUserinfoFriendsList(userID));

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
