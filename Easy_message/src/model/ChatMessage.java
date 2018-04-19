package model;

public class ChatMessage {
    private String senderID=null;
    private String anotherID;
    private byte nature;
    private String sendTime;
    private String message;

    public ChatMessage(String anotherID, byte nature, String sendTime, String message) {
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.message = message;
    }

    public ChatMessage(String senderID, String anotherID, byte nature, String sendTime, String message) {
        this.senderID = senderID;
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getAnotherID() {
        return anotherID;
    }

    public void setAnotherID(String anotherID) {
        this.anotherID = anotherID;
    }

    public byte getNature() {
        return nature;
    }

    public void setNature(byte nature) {
        this.nature = nature;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
