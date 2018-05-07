package model.group;

public class GroupMember {
    private String userID;
    private String userName;
    private byte[] userHeadIcon=null;
    private byte userStatus;

    public GroupMember(String userID, String userName, byte[] userHeadIcon, byte userStatus) {
        this.userID = userID;
        this.userName = userName;
        this.userHeadIcon = userHeadIcon;
        this.userStatus = userStatus;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getUserHeadIcon() {
        return userHeadIcon;
    }

    public void setUserHeadIcon(byte[] userHeadIcon) {
        this.userHeadIcon = userHeadIcon;
    }

    public byte getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(byte userStatus) {
        this.userStatus = userStatus;
    }
}
