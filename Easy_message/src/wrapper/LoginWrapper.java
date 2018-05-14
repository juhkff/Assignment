package wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.message.ContactMessage;
import model.property.User;
import test.Client.Request;
import test.Client.RequestProperty;
import wrapper.thread.GetJsonThread;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static wrapper.StaticVariable.URL_ADDRESS;

/*
 * 代码示例:
 * 1、  String loginResult=LoginWrapper.login(URL_ADDRESS,userID,passWord);
 * 2、  User user=LoginWrapper.getUser(URL_ADDRESS,userID,passWord);
 * 3、  String loginResult=LoginWrapper.userLogin(URL_ADDRESS,userID,passWord);
 *      User user=null;
 *      if(!loginResult.equals("null")&&!loginResult.equals("false")&&!loginResult.equals("error")&&!loginResult.equals("none")){
 *          Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
 *          Type type = new TypeToken<User>(){}.getType();
 *          user = gson.fromJson(loginResult, type);
 *      }
 */
public class LoginWrapper {



    //1、
    /**
     *
     * @param userID
     * @param passWord
     * @return "null" || "false" || "error" || "true"
     */
    /*登录时发送请求**/
    public static String login(String userID, String passWord) throws InterruptedException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userID", userID);
        parameters.put("passWord", passWord);
        Request request = new Request(URL_ADDRESS + "/Login", parameters, RequestProperty.APPLICATION);
        String result = request.doPost();     //获得验证结果

        //开启接收Json文件的线程
        GetJsonThread getJsonThread=new GetJsonThread(userID);
        Thread thread=new Thread(getJsonThread);
        thread.start();
        //这个线程必须执行完毕才能进行下一步
        thread.join();
        return result;
         /*
        返回的result可能的结果:
            if (result.equals("null"))
                System.out.println("帐号不存在，登录失败!");
            else if (result.equals("false"))
                System.out.println("密码错误，登录失败!");
            else if (result.equals("error"))
                System.out.println("发生了未知错误...");
            else if (result.equals("true"))
                System.out.println("帐号密码验证成功!");
        */
    }



    //2、
    /**
     * @param userID
     * @param passWord
     * @return User user || null(nearly impossible)
     * @throws SQLException
     */
    /*获得用户的所有信息**/
    public static User getUser(String userID, String passWord) throws SQLException {
        User user = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userID", userID);
        Request request = new Request(URL_ADDRESS + "/GetUserInfo", parameters, RequestProperty.APPLICATION);
        String userInfo = request.doPost();                                           //userInfo:用户json数据/none
        if (userInfo.equals("none")) {
            throw new SQLException("用户数据查询出错!");
        } else {
            //userInfo即为json格式的数据
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<User>() {
            }.getType();
            user = gson.fromJson(userInfo, type);
            /*加上密码*/
            user.setPassWord(passWord);
            return user;
        }
        /*
        返回User类对象
        */
    }



    //3、
    /**
     * @param userID
     * @param passWord
     * @return "null" || "false" || "error" || "none" || (String)user
     */
    /*这个方法和上面两个方法二选一**/
    /*对上面的两个方法进行合并，这时候返回的user是未经过Gson转化的字符串类型对象**/
    public static String userLogin(String userID, String passWord) {
        String result = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userID", userID);
        parameters.put("passWord", passWord);
        Request request = new Request(URL_ADDRESS + "/UserLogin", parameters, RequestProperty.APPLICATION);
        result = request.doPost();     //获得验证结果
        return result;
         /*
         result可能的值:
            if (result.equals("null"))
                System.out.println("帐号不存在，登录失败!");
            else if (result.equals("false"))
                System.out.println("密码错误，登录失败!");
            else if (result.equals("error"))
                System.out.println("发生了未知错误...");
            else if (result.equals("none"))
                throw new SQLException("用户数据查询出错!");
            else{
                pw.print(gson.toJson(user)); (一切正常时，返回的是用户未经转换的User类对象对应的字符串)
            如果向转换成User类对象，需要手动设置代码:
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<User>() {
                }.getType();
                User user = gson.fromJson(result, type);
            则user即为result经Gson转换后的User类对象
            }
        */
    }

    //获得消息界面左侧的联系人列表
    public static ArrayList<ContactMessage> getContactMessageList(String userID,String exitTime){
        ArrayList<ContactMessage> contactMessages=new ArrayList<ContactMessage>();
        Map<String,String> parameters=new HashMap<String, String>();
        parameters.put("userID",userID);
        parameters.put("exitTime",exitTime);
        Request request=new Request(URL_ADDRESS+"/GetContactMessageList",parameters,RequestProperty.APPLICATION);
        String result=request.doPost();
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<ArrayList<ContactMessage>>(){}.getType();
        contactMessages=gson.fromJson(result,type);
        return contactMessages;
    }

}
