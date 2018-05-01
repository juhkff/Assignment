package model;

import tools.Chat;

import java.io.UnsupportedEncodingException;

public class ChatMessage {
    private String senderID = null;
    private String anotherID;
    private byte nature;
    private String sendTime;
    private String message;
    private String isAccept = "";

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

    public ChatMessage(String senderID, String anotherID, byte nature, String sendTime, String message, String isAccept) {
        this.senderID = senderID;
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.message = message;
        this.isAccept = isAccept;
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

    public String getMessage() throws UnsupportedEncodingException {
        return Chat.decodeChinese(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIsAccept() {
        return isAccept;
    }

    public void setIsAccept(String isAccept) {
        this.isAccept = isAccept;
    }
}
