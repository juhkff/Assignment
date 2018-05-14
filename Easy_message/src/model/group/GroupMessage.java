package model.group;

public class GroupMessage {
    private String groupID=null;
//    private String groupName;
    private String senderID;
    private String senderName;
    private String sendTime;
    private byte Status;
    private String Content=null;
    private byte[] Img=null;
    private byte ifSuccess=0;

    public GroupMessage(String groupID,String senderID, String senderName, String sendTime, byte status, String content, byte ifSuccess) {
        this.groupID=groupID;
        this.senderID = senderID;
        this.senderName = senderName;
        this.sendTime = sendTime;
        Status = status;
        Content = content;
        this.ifSuccess = ifSuccess;
    }

    public GroupMessage(String groupID,String senderID, String senderName, String sendTime, byte status, byte[] img, byte ifSuccess) {
        this.groupID=groupID;
        this.senderID = senderID;
        this.senderName = senderName;
        this.sendTime = sendTime;
        Status = status;
        Img = img;
        this.ifSuccess = ifSuccess;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public byte getStatus() {
        return Status;
    }

    public void setStatus(byte status) {
        Status = status;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public byte[] getImg() {
        return Img;
    }

    public void setImg(byte[] img) {
        Img = img;
    }

    public byte getIfSuccess() {
        return ifSuccess;
    }

    public void setIfSuccess(byte ifSuccess) {
        this.ifSuccess = ifSuccess;
    }
}
