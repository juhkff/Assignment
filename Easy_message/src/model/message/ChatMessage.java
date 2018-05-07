package model.message;

import tools.Chat;

import java.io.UnsupportedEncodingException;

public class ChatMessage {
    private String senderID;
    private String anotherID;               /*/**anotherID即是senderID*(误)*/
    private byte nature;
    private String sendTime;
    private String message=null;
    private byte[] img=null;                     /**聊天图片**/
    private String isAccept = "";

    public ChatMessage(String anotherID, byte nature, String sendTime, String message) {
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.message = message;
    }

    public ChatMessage(String anotherID, byte nature, String sendTime, byte[] img) {
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.img = img;
    }

    public ChatMessage(String senderID, String anotherID, byte nature, String sendTime, String message) {
        this.senderID = senderID;
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.message = message;
    }

    public ChatMessage(String senderID, String anotherID, byte nature, String sendTime, byte[] img) {
        this.senderID = senderID;
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.img = img;
    }

    public ChatMessage(String anotherID, byte nature, String sendTime, String message, String isAccept) {
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.message = message;
        this.isAccept = isAccept;
    }

    public ChatMessage(String anotherID, byte nature, String sendTime, byte[] img, String isAccept) {
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.img = img;
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

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public String getIsAccept() {
        return isAccept;
    }

    public void setIsAccept(String isAccept) {
        this.isAccept = isAccept;
    }
}
