package model.file;

public class MyFileInfo {
    private String userID;
    private String fileName;
    private byte[] filebytes;

    public MyFileInfo(String userID, String fileName, byte[] filebytes) {
        this.userID = userID;
        this.fileName = fileName;
        this.filebytes = filebytes;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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
