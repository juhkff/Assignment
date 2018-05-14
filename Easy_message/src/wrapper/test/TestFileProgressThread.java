package wrapper.test;

import model.file.FileProgress;

import java.util.concurrent.BlockingQueue;

public class TestFileProgressThread implements Runnable{
    private BlockingQueue<FileProgress> fileProgresses;

    public TestFileProgressThread(BlockingQueue<FileProgress> fileProgresses) {
        this.fileProgresses = fileProgresses;
    }

    @Override
    public void run() {
        FileProgress fileProgress = null;
        while (true) {
            try {
                fileProgress = fileProgresses.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("目前发送/接收:"+fileProgress.getCurrentSize()+"Bytes"+"   总大小:"+fileProgress.getTotalSize()+"Bytes"+"\t进度:"+fileProgress.getCurrentPer());
        }
    }
}
