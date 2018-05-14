package wrapper.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.group.GroupMember;
import model.group.GroupMessage;
import model.group.SimpleGroup;
import test.Client.Request;
import test.Client.RequestProperty;
import wrapper.ThreadWrapper;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.URL_ADDRESS;

public class GroupWrapper {
    public static void joinGroup(String userID, String groupID, String nickName, String headIconTrans, String exitTime, BlockingQueue<GroupMessage> groupMessages) {
        Map<String, String> parameters30 = new HashMap<String, String>();
//        parameters30.put("URL_ADDRESS", URL_ADDRESS);
        parameters30.put("userID", userID);
        parameters30.put("groupID", groupID);
        parameters30.put("userName", nickName);
        parameters30.put("userHeadIconTrans", headIconTrans);
        Request request30 = new Request(URL_ADDRESS + "/JoinGroup", parameters30, RequestProperty.APPLICATION);
        /**客户端获得的返回值是SimpleGroup对象**/
        String result30 = request30.doPost();
//                    System.out.println(result30);
        Gson gson31 = new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type31 = new TypeToken<SimpleGroup>() {
        }.getType();
        SimpleGroup simpleGroup = gson31.fromJson(result30, type31);

        /**添加监听此群的线程**/
        ThreadWrapper.GroupListenerThread groupListenerThread = new ThreadWrapper.GroupListenerThread(userID, groupID, exitTime,groupMessages);
        Thread thread30 = new Thread(groupListenerThread);
        thread30.start();
    }
}
