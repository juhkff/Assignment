package model.file;

public class FileProgress {
    private long currentSize;                                         //目前已接收文件的大小(单位是字节)
    private long totalSize;                                           //接收的文件的总大小
    private int currentPercent;                                         //接收进度百分比数值
    private String currentPer;                                          //接收进度百分比

    public FileProgress(long currentSize, long totalSize, int currentPercent) {
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.currentPercent = currentPercent;
        this.currentPer = currentPercent+"%";
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getCurrentPercent() {
        return currentPercent;
    }

    public void setCurrentPercent(int currentPercent) {
        this.currentPercent = currentPercent;
    }

    public String getCurrentPer() {
        return currentPer;
    }

    public void setCurrentPer(String currentPer) {
        this.currentPer = currentPer;
    }
}
