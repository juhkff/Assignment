package p2pserver.socket;

import p2pserver.fileServerDao.TransferThread;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSocket {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;

        serverSocket = new ServerSocket(3333);
        while (true) {
            socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            if (dataInputStream.readUTF().equals("Transfer")) {
                //为每个用户设置一个线程
                TransferThread uploadThread = new TransferThread(socket,inputStream);
                Thread thread = new Thread(uploadThread);
                thread.start();
            }
        }
    }
}
