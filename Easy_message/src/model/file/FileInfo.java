package model.file;

public class FileInfo {
    private String senderID;
    private String receiverID;
    private String fileName;
    private byte[] filebytes;

    public FileInfo(String senderID, String receiverID, String fileName,byte[] filebytes) {
        super();
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.fileName=fileName;
        this.filebytes = filebytes;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFilebytes() {
        return filebytes;
    }

    public void setFilebytes(byte[] filebytes) {
        this.filebytes = filebytes;
    }


}
