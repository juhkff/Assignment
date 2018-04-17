package model;

public class Contact {
    private String ID;
    private String nickName;
    private byte[] headIcon;
    private byte types;
    private boolean status;
    private boolean isupdate;

    public Contact(String ID, String nickName, byte[] headIcon, byte types, boolean status, boolean isupdate) {
        this.ID = ID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.types = types;
        this.status = status;
        this.isupdate = isupdate;
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

    public byte isTypes() {
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

    public boolean isIsupdate() {
        return isupdate;
    }

    public void setIsupdate(boolean isupdate) {
        this.isupdate = isupdate;
    }
}


