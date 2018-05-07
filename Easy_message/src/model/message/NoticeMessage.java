package model.message;

public class NoticeMessage {
    private String anotherID;
    private String nickName;
    private byte property;

    public NoticeMessage(String anotherID, String nickName, byte property) {
        this.anotherID = anotherID;
        this.nickName = nickName;
        this.property = property;
    }

    public String getAnotherID() {
        return anotherID;
    }

    public void setAnotherID(String anotherID) {
        this.anotherID = anotherID;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public byte getProperty() {
        return property;
    }

    public void setProperty(byte property) {
        this.property = property;
    }
}
