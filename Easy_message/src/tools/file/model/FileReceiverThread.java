package tools.file.model;

import model.file.FileProgress;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;

public class FileReceiverThread implements Runnable {
    private final String SAVE_FILE_PATH;
//    private long fileSize;
    private BlockingQueue<FileProgress> fileProgresses;

    public FileReceiverThread(String SAVE_FILE_PATH, BlockingQueue<FileProgress> fileProgresses) {
        this.SAVE_FILE_PATH = SAVE_FILE_PATH;
        this.fileProgresses = fileProgresses;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(5555);
//            while (true){
            socket = serverSocket.accept();
            AcceptThread acceptThread = new AcceptThread(socket, fileProgresses, SAVE_FILE_PATH);
            Thread thread = new Thread(acceptThread);
            thread.start();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
