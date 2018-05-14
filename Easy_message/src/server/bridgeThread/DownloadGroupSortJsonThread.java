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

public class DownloadGroupSortJsonThread implements Runnable{
    private BlockingQueue<ServerCommand> groupSortJsonCommand;

    public DownloadGroupSortJsonThread(BlockingQueue<ServerCommand> groupSortJsonCommand) {
        this.groupSortJsonCommand = groupSortJsonCommand;
    }

    @Override
    public void run() {
        while (true){
            try {
                ServerCommand serverCommand = groupSortJsonCommand.take();
                String userID=serverCommand.getUserID();
                Socket socket=socketMap.get(userID);
                OutputStream outputStream=socket.getOutputStream();
                DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
                dataOutputStream.writeUTF("SortGroups");
                File file=new File(StaticVariable.getUserinfoSortGroups(userID));

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
