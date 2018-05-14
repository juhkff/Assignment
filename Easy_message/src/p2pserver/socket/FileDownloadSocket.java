package p2pserver.socket;

import com.mysql.fabric.Server;
import p2pserver.fileServerDao.DownloadFileThread;
import test.Client.DownloadFileRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static wrapper.StaticVariable.FILE_SERVER_SOCKET_DOWNLOAD_PORT;

public class FileDownloadSocket {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket=null;
        Socket socket=null;
        serverSocket=new ServerSocket(FILE_SERVER_SOCKET_DOWNLOAD_PORT);        //4444
        while (true){
            socket=serverSocket.accept();
            DownloadFileThread downloadFileThread=new DownloadFileThread(socket);
            Thread thread=new Thread(downloadFileThread);
            thread.start();
        }
    }
}
