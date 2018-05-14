package model.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import test.Client.Request;

import java.lang.reflect.Type;
import java.util.*;

public class Group {
    private String URL_ADDRESS;
    private String groupID;
    private String groupName;
    private byte[] groupIcon = null;
    private String theLatestMessage = null;
    private String theLatestSendTime = null;
    /**
     * 额外内容
     **/
    private String groupIntro = null;
    private String creatorID;
    private int managerNumber = 0;
    private int groupNumber = 1;
    private String createTime;
    private GroupMembers members;

    public Group(String URL_ADDRESS, String groupID, String groupName, byte[] groupIcon, String theLatestMessage, String theLatestSendTime, String groupIntro, String creatorID, int managerNumber, int groupNumber, String createTime) {
        this.URL_ADDRESS = URL_ADDRESS;
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupIcon = groupIcon;
        this.theLatestMessage = theLatestMessage;
        this.theLatestSendTime = theLatestSendTime;
        this.groupIntro = groupIntro;
        this.creatorID = creatorID;
        this.managerNumber = managerNumber;
        this.groupNumber = groupNumber;
        this.createTime = createTime;
        this.members = new GroupMembers(this.URL_ADDRESS, this.groupID);
    }

    /**
     * 内部类(群成员)
     **/
    private class GroupMembers {
        private Map<String, GroupMember> members;

        public GroupMembers(String URL_ADDRESS, String groupID) {
            String THE_URL_ADDRESS = URL_ADDRESS + "/GetGroupMembers";
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("groupID", groupID);
            Request request = new Request(THE_URL_ADDRESS, parameters, "application/x-www-form-urlencoded");        //为什么这里无法用RequestProperty类?
            String members_Trans = request.doPost();
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<Map<String, GroupMember>>() {
            }.getType();
            /**得到成员列表**/
            this.members = gson.fromJson(members_Trans, type);
        }

        public ArrayList<GroupMember> getMemberList(){
            ArrayList<GroupMember> groupMemberArrayList=new ArrayList<GroupMember>();
            Set<String> stringSet=members.keySet();
            Iterator<String> iterator=stringSet.iterator();
            while (iterator.hasNext()){
                String ID=iterator.next();
                GroupMember groupMember=members.get(ID);
                groupMemberArrayList.add(groupMember);
            }

            return groupMemberArrayList;
        }

        public Map<String,GroupMember> getMemberMap(){
            return members;
        }

        public GroupMember getMember(String memberID) {
            return members.get(memberID);
        }

        @Override
        public String toString() {
//            return super.toString();
            Set<String> groupMemberSet = this.members.keySet();
            Iterator<String> iterator = groupMemberSet.iterator();
//            System.out.println("\n成员列表:\n");
            String result = "\n成员列表:";
            while (iterator.hasNext()) {
                String userID = iterator.next();
                GroupMember groupMember = this.members.get(userID);
                byte status = groupMember.getUserStatus();
                String userStatus = "查询错误!";
                if (status == 0)
                    userStatus = "群员";
                else if (status == 1)
                    userStatus = "管理员";
                else if (status == 2)
                    userStatus = "群主";
                result += "\nID: " + groupMember.getUserID() +
                        "\t昵称: " + groupMember.getUserName() +
                        "\t头像: " +/*groupMember.getUserHeadIcon()*/"无法显示" +
                        "\t身份: " + userStatus +
                        "\t在线状态: " + (groupMember.isUserOnline()?"在线":"下线")
                ;
            }
            return result;
        }
    }

    public String showMembers(){
        return this.members.toString();
    }

    public GroupMember getGroupMember(String memberID) {
        return members.getMember(memberID);
    }

    public void replaceGroupMemver(GroupMember groupMember){
        getGroupMember(groupMember.getUserID()).setUserName(groupMember.getUserName());
        getGroupMember(groupMember.getUserID()).setUserHeadIcon(groupMember.getUserHeadIcon());
        getGroupMember(groupMember.getUserID()).setUserStatus(groupMember.getUserStatus());
        getGroupMember(groupMember.getUserID()).setUserOnline(groupMember.isUserOnline());
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

    public String getGroupIntro() {
        return groupIntro;
    }

    public void setGroupIntro(String groupIntro) {
        this.groupIntro = groupIntro;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public int getManagerNumber() {
        return managerNumber;
    }

    public void setManagerNumber(int managerNumber) {
        this.managerNumber = managerNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
