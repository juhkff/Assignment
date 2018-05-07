package model.group;

import java.util.ArrayList;

public class SimpleGroup {
    private String groupID;
    private String groupName;
    private byte[] groupIcon=null;

    private String theLatestMessage=null;
    private String theLatestSendTime=null;
    //private ArrayList<Contact> groupMemberList=new ArrayList<Contact>();

    /*private String theLatestText = null;
    private String theLatestTextTime = null;
    */

    public SimpleGroup(String groupID, String groupName) {
        this.groupID = groupID;
        this.groupName = groupName;
    }

    public SimpleGroup(String groupID, String groupName, byte[] groupIcon) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupIcon = groupIcon;
    }

    public SimpleGroup(String groupID, String groupName, byte[] groupIcon, String theLatestMessage, String theLatestSendTime) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupIcon = groupIcon;
        this.theLatestMessage = theLatestMessage;
        this.theLatestSendTime = theLatestSendTime;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public byte[] getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(byte[] groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getTheLatestMessage() {
        return theLatestMessage;
    }

    public void setTheLatestMessage(String theLatestMessage) {
        this.theLatestMessage = theLatestMessage;
    }

    public String getTheLatestSendTime() {
        return theLatestSendTime;
    }

    public void setTheLatestSendTime(String theLatestSendTime) {
        this.theLatestSendTime = theLatestSendTime;
    }
}
