package model.message;

import tools.Chat;

import java.io.UnsupportedEncodingException;

public class ContactMessage {
    private String userID;
    private String nickName;
    private byte[] headIcon;
    private String theLattestmessage=null;
    private int messageNum=0;

    public ContactMessage(String userID, String nickName, byte[] headIcon, String theLattestmessage) {
        this.userID = userID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.theLattestmessage = theLattestmessage;
    }

    public ContactMessage(String userID, String nickName, byte[] headIcon, String theLattestmessage, int messageNum) {
        this.userID = userID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.theLattestmessage = theLattestmessage;
        this.messageNum = messageNum;
    }

    public ContactMessage(String userID, String nickName, byte[] headIcon, int messageNum) {
        this.userID = userID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.messageNum = messageNum;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public byte[] getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(byte[] headIcon) {
        this.headIcon = headIcon;
    }

    public String getTheLattestmessage() {
//        return theLattestmessage;
        try {
            return Chat.decodeChinese(theLattestmessage);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "error!";
    }

    public void setTheLattestmessage(String theLattestmessage) {
        this.theLattestmessage = theLattestmessage;
    }

    public int getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(int messageNum) {
        this.messageNum = messageNum;
    }
}
