package model.contact;

import tools.Chat;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

public class Contact {
    private String ID;
    private String nickName;
    private byte[] headIcon = null;

    /**
     * 新加
     **/
    private String intro=null;
    private Boolean isMale=null;
    private String email=null;
    private String phoneNum=null;
    private String exitTime=null;

    private byte types;//不需要
    private Boolean status;//上下线
    private String theLatestText = null;
    private String theLatestTextTime = null;
    //private boolean isupdate;

    public Contact(String ID, String nickName, byte[] headIcon, byte types, Boolean status/*, boolean isupdate*/) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.types = types;
        this.status = status;
        //this.isupdate = isupdate;
    }

    public Contact(String ID, String nickName, byte[] headIcon, String intro, Boolean isMale, byte types, Boolean status) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.intro = intro;
        this.isMale = isMale;
        this.types = types;
        this.status = status;
    }

    public Contact(String ID, String nickName, byte[] headIcon, byte types, Boolean status, String theLatestText, String theLatestTextTime) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.types = types;
        this.status = status;
        this.theLatestText = theLatestText;
        this.theLatestTextTime = theLatestTextTime;
    }

    public Contact(String ID, String nickName, byte[] headIcon, String intro, Boolean isMale, String email, String phoneNum, String exitTime, byte types, boolean status, String theLatestText, String theLatestTextTime) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.intro = intro;
        this.isMale = isMale;
        this.email = email;
        this.phoneNum = phoneNum;
        this.exitTime = exitTime;
        this.types = types;
        this.status = status;
        this.theLatestText = theLatestText;
        this.theLatestTextTime = theLatestTextTime;
    }

    public String getTheLatestTextTime() {
        return theLatestTextTime;
    }

    public void setTheLatestTextTime(String theLatestTextTime) {
        this.theLatestTextTime = theLatestTextTime;
    }

    public String getTheLatestText() throws UnsupportedEncodingException {
        if(theLatestText!=null)
            return Chat.decodeChinese(theLatestText);
        else
            return null;
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

    public Boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Boolean isMale() {
        return isMale;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getExitTime() {
        return exitTime;
    }

    public String getIntro() {
        return intro;
    }

    /*public boolean isIsupdate() {
        return isupdate;
    }

    public void setIsupdate(boolean isupdate) {
        this.isupdate = isupdate;
    }*/
}


