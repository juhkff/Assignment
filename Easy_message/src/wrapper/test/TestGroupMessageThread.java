package wrapper.test;

import model.group.GroupMember;
import model.group.GroupMessage;

import java.util.concurrent.BlockingQueue;

//import static wrapper.StaticVariable.groups;

public class TestGroupMessageThread implements Runnable{
    private BlockingQueue<GroupMessage> groupMessages;

    public TestGroupMessageThread(BlockingQueue<GroupMessage> groupMessages) {
        this.groupMessages = groupMessages;
    }

    @Override
    public void run() {
        while (true){
            try {
                GroupMessage groupMessage=groupMessages.take();
                String groupID=groupMessage.getGroupID();
                /**PS:发送者头像是通过调用获取Group内部类指定ID的对象的方法，并调用此对象的getUserHeadIcon()方法获得的**/
                if (groupMessage.getStatus() == 0) {
                    if (groupMessage.getContent() != null)
                        System.out.println("From: " + groupID + "发送者头像:(无法显示)" + groupMessage.getSenderName() + " : " + groupMessage.getContent() + "(" + groupMessage.getSendTime() + ")");
                    else if (groupMessage.getImg() != null)
                        System.out.println("From: " + groupID +"发送者头像:(无法显示)" + groupMessage.getSenderName() + " : " + "发送的图片:" +/*groupMessage.getImg()*/"(无法显示)" + "(" + groupMessage.getSendTime() + ")");
                } else if (groupMessage.getStatus() == 1) {
                    System.out.println("From: " + groupID + "文件 From: " + groupMessage.getSenderName() + "发送者头像: (无法显示)"/*+(group.getGroupMember(groupMessage.getSenderID()).getUserHeadIcon())*/ + "\tName: " + groupMessage.getContent());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
