package model;

import java.sql.Timestamp;

public class Contact {
    private String ID;
    private String nickName;
    private byte[] headIcon;
    private byte types;
    private boolean status;
    private String theLatestText=null;
    private String theLatestTextTime=null;
    //private boolean isupdate;

    public Contact(String ID, String nickName, byte[] headIcon, byte types, boolean status/*, boolean isupdate*/) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.types = types;
        this.status = status;
        //this.isupdate = isupdate;
    }

    public Contact(String ID, String nickName, byte[] headIcon, byte types, boolean status, String theLatestText,String theLatestTextTime) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.types = types;
        this.status = status;
        this.theLatestText = theLatestText;
        this.theLatestTextTime=theLatestTextTime;
    }

    public String getTheLatestTextTime() {
        return theLatestTextTime;
    }

    public void setTheLatestTextTime(String theLatestTextTime) {
        this.theLatestTextTime = theLatestTextTime;
    }

    public String getTheLatestText() {
        return theLatestText;
    }

    public void setTheLatestText(String theLatestText) {
        this.theLatestText = theLatestText;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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

    public byte getTypes() {
        return types;
    }

    public void setTypes(byte types) {
        this.types = types;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /*public boolean isIsupdate() {
        return isupdate;
    }

    public void setIsupdate(boolean isupdate) {
        this.isupdate = isupdate;
    }*/
}


