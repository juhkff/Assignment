package server.bridgeThread;

import server.model.ServerCommand;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class CommandThread implements Runnable{
    private BlockingQueue<ServerCommand> friendJsonCommand;
    private BlockingQueue<ServerCommand> friendSortJsonCommand;
    private BlockingQueue<ServerCommand> groupJsonCommand;
    private BlockingQueue<ServerCommand> groupSortJsonCommand;

    public CommandThread(BlockingQueue<ServerCommand> friendJsonCommand, BlockingQueue<ServerCommand> friendSortJsonCommand, BlockingQueue<ServerCommand> groupJsonCommand, BlockingQueue<ServerCommand> groupSortJsonCommand) {
        this.friendJsonCommand = friendJsonCommand;
        this.friendSortJsonCommand = friendSortJsonCommand;
        this.groupJsonCommand = groupJsonCommand;
        this.groupSortJsonCommand = groupSortJsonCommand;
    }

    @Override
    public void run() {
        DownLoadFriendJsonThread downLoadFriendJsonThread=new DownLoadFriendJsonThread(friendJsonCommand);
        Thread thread=new Thread(downLoadFriendJsonThread);
        thread.start();

        DownloadFriendSortJsonThread downloadFriendSortJsonThread=new DownloadFriendSortJsonThread(friendSortJsonCommand);
        Thread thread1=new Thread(downloadFriendSortJsonThread);
        thread1.start();

        DownloadGroupJsonThread downloadGroupJsonThread=new DownloadGroupJsonThread(groupJsonCommand);
        Thread thread2=new Thread(downloadGroupJsonThread);
        thread2.start();

        DownloadGroupSortJsonThread downloadGroupSortJsonThread=new DownloadGroupSortJsonThread(groupSortJsonCommand);
        Thread thread3=new Thread(downloadGroupSortJsonThread);
        thread3.start();
    }
}
