package server.bridgeThread;

import server.jsonupdate.ResponseThread;
import server.model.ServerCommand;
import wrapper.StaticVariable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class JsonServer implements Runnable{
    private BlockingQueue<ServerCommand> friendJsonCommand;
    private BlockingQueue<ServerCommand> friendSortJsonCommand;
    private BlockingQueue<ServerCommand> groupJsonCommand;
    private BlockingQueue<ServerCommand> groupSortJsonCommand;

    public JsonServer(BlockingQueue<ServerCommand> friendJsonCommand, BlockingQueue<ServerCommand> friendSortJsonCommand, BlockingQueue<ServerCommand> groupJsonCommand, BlockingQueue<ServerCommand> groupSortJsonCommand) {
        this.friendJsonCommand = friendJsonCommand;
        this.friendSortJsonCommand = friendSortJsonCommand;
        this.groupJsonCommand = groupJsonCommand;
        this.groupSortJsonCommand = groupSortJsonCommand;
    }

    @Override
    public void run() {
        ServerSocket serverSocket=null;
        Socket socket=null;
        try {
            serverSocket=new ServerSocket(StaticVariable.FILE_SERVER_SOCKET_JSON_PORT);                //6666用于给客户端发送Json文件
            while (true){
                socket=serverSocket.accept();
                ResponseThread responseThread=new ResponseThread(socket,friendJsonCommand,friendSortJsonCommand,groupJsonCommand,groupSortJsonCommand);
                Thread thread=new Thread(responseThread);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
