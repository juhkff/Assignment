package servlets.online.put;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.property.User;
import tools.Online;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;

@WebServlet(name = "CommitChangeServlet",urlPatterns = "/CommitChange")
public class CommitChangeServlet extends HttpServlet {
    public static void main(String[] args) throws IOException {
        InputStream inputStream=new FileInputStream("D:\\aa.jpg");
        byte[] bytes=null;
        if (inputStream!=null){
            bytes=new byte[inputStream.available()];
            inputStream.read(bytes,0,inputStream.available());
            inputStream.close();
        }
        User user=new User("8133523681","Aqko251068","juhkgf",bytes,true,"363257597@qq.com",
                "17860536820","2017-02-06 23:41:55","Come on!");
        String commitResult=Online.commitUserInfo(user);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userInfo=request.getParameter("user");
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<User>(){}.getType();
        User user=gson.fromJson(userInfo,type);
        /*得到用户对象*/

        String commitResult=Online.commitUserInfo(user);

        PrintWriter printWriter=response.getWriter();
        printWriter.print(commitResult);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
