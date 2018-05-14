package model.message;

public class FileMessage {
    public static void main(String[] args){
        String fileName="F:\\FFOutput\\left4dead2 2018-03-10 01-58-52-660~1.mp4";
        fileName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
        System.out.println(fileName);
    }
    private String senderID;
    private String senderNickName=null;
    private String receiverID;
    private String receiverNickName=null;
    private String fileName;
    private String fileSize;


    public FileMessage(String senderID, String receiverID, String fileName, String fileSize) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileMessage(String senderID, String senderNickName, String receiverID, String receiverNickName, String fileName, String fileSize) {
        this.senderID = senderID;
        this.senderNickName = senderNickName;
        this.receiverID = receiverID;
        this.receiverNickName = receiverNickName;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }


    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderNickName() {
        return senderNickName;
    }

    public void setSenderNickName(String senderNickName) {
        this.senderNickName = senderNickName;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getReceiverNickName() {
        return receiverNickName;
    }

    public void setReceiverNickName(String receiverNickName) {
        this.receiverNickName = receiverNickName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
