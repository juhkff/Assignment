package servlets.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.group.Group;
import test.Client.LoginClient;
import wrapper.StaticVariable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import static wrapper.StaticVariable.*;

@WebServlet(name = "GetFullGroupServlet",urlPatterns = "/GetFullGroup")
public class GetFullGroupServlet extends HttpServlet {

    //测试Gson能不能深转换对象
    //测试Gson能不能深转换对象
    static class Test{
        private String name;
        private byte[] testBytes;

        public Test(String name, byte[] testBytes) {
            this.name = name;
            this.testBytes = testBytes;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getTestBytes() {
            return testBytes;
        }

        public void setTestBytes(byte[] testBytes) {
            this.testBytes = testBytes;
        }
    }
    public static void main(String[] args){
        /*byte[] test="TestGsonToBytes".getBytes();
        String name="A test";

        Test test1=new Test(name,test);

        //模拟发送数组给servlet
        Gson gson=new Gson();
        String trans_1=gson.toJson(test);
        System.out.println("1: "+trans_1);        //servlet显示转换后的数组

        //servlet转换格式
        Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();

        //模拟servlet转换数组为数组格式
        byte[] trans_bytes=gson1.fromJson(trans_1,type);
        System.out.println("2: "+trans_bytes+"\t字符串:"+new String(trans_bytes) );

        Test test2=new Test("Name",trans_bytes);
        Gson gson2=new Gson();
        String trans_2=gson2.toJson(test2);
        System.out.println("3: "+trans_2);            *//** byte[] 类型会如何转换? **//*

        Gson gson3=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type3=new TypeToken<Test>(){}.getType();

        *//**模拟客户端接收servlet发送的含byte[](未转换,只转换了Test对象)类型**//*
        Test test3=gson3.fromJson(trans_2,type3);
        System.out.println("4: Name: "+test3.getName());
        System.out.println("5: byte[]: "+test3.getTestBytes()+"\t字符串: "+new String(test3.getTestBytes()));
        *//**最佳结果: 获得与trans_bytes即2中相同的结果...**//*
        System.out.println("最佳结果: 获得的5与trans_bytes即2中相同的结果...");*/

        String URL_ADDRESS=StaticVariable.URL_ADDRESS;
        String groupID="950876";
        String groupName="Test";
        byte[] groupIcon=null;
        String theLatestMessage=null;
        String theLatestSendTime=null;

        /**获得完整群对象**/
        Group group= tools.Group.getFullGroup(URL_ADDRESS,groupID,groupName,groupIcon,theLatestMessage,theLatestSendTime);
        Gson gson1=new Gson();
//        PrintWriter printWriter=response.getWriter();
        String group_Trans=gson1.toJson(group);
        System.out.println(group_Trans);

    }
    //结果:能够深转换对象(及深解析字符串获得类对象)

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String URL_ADDRESS=request.getParameter("URL_ADDRESS");
        String groupID=request.getParameter("groupID");
        String groupName=request.getParameter("groupName");
        String groupIcon_Trans=request.getParameter("groupIcon");
        String theLatestMessage=request.getParameter("theLatestMessage");
        String theLatestSendTime=request.getParameter("theLatestSendTime");

        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();

        byte[] groupIcon=gson.fromJson(groupIcon_Trans,type);

        /**获得完整群对象**/
        Group group= tools.Group.getFullGroup(URL_ADDRESS,groupID,groupName,groupIcon,theLatestMessage,theLatestSendTime);
        Gson gson1=new Gson();
        PrintWriter printWriter=response.getWriter();
        String group_Trans=gson1.toJson(group);
        printWriter.print(group_Trans);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
