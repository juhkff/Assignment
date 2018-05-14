package server;

import server.bridgeThread.CommandThread;
import server.bridgeThread.JsonListenerMain;
import server.bridgeThread.JsonServer;
import server.model.ServerCommand;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerStart {
    public static void main(String[] args){
        BlockingQueue<ServerCommand> friendJsonCommand=new ArrayBlockingQueue<ServerCommand>(1);
        BlockingQueue<ServerCommand> friendSortJsonCommand=new ArrayBlockingQueue<ServerCommand>(1);
        BlockingQueue<ServerCommand> groupJsonCommand=new ArrayBlockingQueue<ServerCommand>(1);
        BlockingQueue<ServerCommand> groupSortJsonCommand=new ArrayBlockingQueue<ServerCommand>(1);

        CommandThread commandThread=new CommandThread(friendJsonCommand,friendSortJsonCommand,groupJsonCommand,groupSortJsonCommand);
        Thread thread=new Thread(commandThread);
        thread.start();

        JsonServer jsonServer=new JsonServer(friendJsonCommand,friendSortJsonCommand,groupJsonCommand,groupSortJsonCommand);
        Thread thread1=new Thread(jsonServer);
        thread1.start();


        JsonListenerMain jsonListenerMain=new JsonListenerMain(friendJsonCommand,friendSortJsonCommand,groupJsonCommand,groupSortJsonCommand);
        Thread thread2=new Thread(jsonListenerMain);
        thread2.start();
    }
}
