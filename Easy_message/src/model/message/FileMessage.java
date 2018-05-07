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
    private String senderAddress;
    private String receiverAddress;
    private String senderFileAddress;
    private String receiverFileAddress;
    private String fileName;
    private String fileSize;


    public FileMessage(String senderID, String receiverID, String senderAddress, String receiverAddress, String fileName, String fileSize) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileMessage(String senderID, String senderNickName, String receiverID, String receiverNickName, String senderAddress, String receiverAddress, String fileName, String fileSize) {
        this.senderID = senderID;
        this.senderNickName = senderNickName;
        this.receiverID = receiverID;
        this.receiverNickName = receiverNickName;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileMessage(String senderID, String senderNickName, String receiverID, String receiverNickName, String senderAddress, String receiverAddress, String senderFileAddress, String receiverFileAddress, String fileName, String fileSize) {
        this.senderID = senderID;
        this.senderNickName = senderNickName;
        this.receiverID = receiverID;
        this.receiverNickName = receiverNickName;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.senderFileAddress = senderFileAddress;
        this.receiverFileAddress = receiverFileAddress;
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

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getSenderFileAddress() {
        return senderFileAddress;
    }

    public void setSenderFileAddress(String senderFileAddress) {
        this.senderFileAddress = senderFileAddress;
    }

    public String getReceiverFileAddress() {
        return receiverFileAddress;
    }

    public void setReceiverFileAddress(String receiverFileAddress) {
        this.receiverFileAddress = receiverFileAddress;
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
