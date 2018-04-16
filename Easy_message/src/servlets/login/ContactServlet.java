package servlets.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tools.Login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ContactServlet",urlPatterns = "/ContactList")
public class ContactServlet extends HttpServlet {
    public static void main(String[] args){
        String userID = "4965757872";
        Map<String,String> contactList;
        try {
            contactList=Login.getContactList(userID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    Map<String,String> contactList;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID=request.getParameter("userID");
        try {
            contactList= Login.getContactList(userID);
        } catch (SQLException e) {
            System.out.println("获取联系人列表失败!servlets.login.ContactServlet");
            e.printStackTrace();
        }
        if(contactList!=null){
            Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();                    //创建Gson对象
            PrintWriter pw=response.getWriter();
            pw.print(gson.toJson(contactList));                                                         //向前端发送Gson对象
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
