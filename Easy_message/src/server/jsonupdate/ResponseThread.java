package server.jsonupdate;

import server.bridgeThread.CommandThread;
import server.model.ServerCommand;
import server.staticvariable.ServerStatic;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static server.staticvariable.ServerStatic.socketMap;

public class ResponseThread implements Runnable{
    private Socket socket;
    private BlockingQueue<ServerCommand> friendJsonCommand;
    private BlockingQueue<ServerCommand> friendSortJsonCommand;
    private BlockingQueue<ServerCommand> groupJsonCommand;
    private BlockingQueue<ServerCommand> groupSortJsonCommand;

    public ResponseThread(Socket socket, BlockingQueue<ServerCommand> friendJsonCommand, BlockingQueue<ServerCommand> friendSortJsonCommand, BlockingQueue<ServerCommand> groupJsonCommand, BlockingQueue<ServerCommand> groupSortJsonCommand) {
        this.socket = socket;
        this.friendJsonCommand = friendJsonCommand;
        this.friendSortJsonCommand = friendSortJsonCommand;
        this.groupJsonCommand = groupJsonCommand;
        this.groupSortJsonCommand = groupSortJsonCommand;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream=socket.getInputStream();
            DataInputStream dataInputStream=new DataInputStream(inputStream);
            String request=dataInputStream.readUTF();
            if(request.equals("Download")){
                //下载四个Json文件的请求
                DownloadJsonThread downloadJsonThread=new DownloadJsonThread(socket);
                Thread thread=new Thread(downloadJsonThread);
                thread.start();
            }
            if(request.equals("Listener")){
                String userID=dataInputStream.readUTF();
                if(socketMap.containsKey(userID)){
                    socketMap.replace(userID,socket);
                }else {
                    socketMap.put(userID, socket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
