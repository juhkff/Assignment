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

public class DownloadFriendSortJsonThread implements Runnable{
    private BlockingQueue<ServerCommand> friendSortJsonCommand;

    public DownloadFriendSortJsonThread(BlockingQueue<ServerCommand> friendSortJsonCommand) {
        this.friendSortJsonCommand = friendSortJsonCommand;
    }

    @Override
    public void run() {
        while (true){
            try {
                ServerCommand  serverCommand = friendSortJsonCommand.take();
                String userID=serverCommand.getUserID();
                Socket socket=socketMap.get(userID);
                OutputStream outputStream=socket.getOutputStream();
                DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                dataOutputStream.writeUTF("SortFriends");
                File file=new File(StaticVariable.getUserinfoSortFriends(userID));

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
