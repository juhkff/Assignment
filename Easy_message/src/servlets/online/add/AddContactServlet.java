package servlets.online.add;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.contact.Contact;
import tools.Online;

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

@WebServlet(name = "AddContactServlet", urlPatterns = "/AddContact")
public class AddContactServlet extends HttpServlet {
    public static void main(String[] args){
        String userID="2461247724";

        Map<String, Contact> userList = null;
        try {
            userList = Online.getAddList(userID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String result = "";
        if (userList.size() > 0) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            result = gson.toJson(userList);
        }
        System.out.println(result);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID = request.getParameter("userID");
        Map<String, Contact> userList = new HashMap<>();
        try {
            userList = Online.getAddList(userID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String result = "";
        Gson gson = new Gson();
        if (userList.size() > 0) {
            result = gson.toJson(userList);
        }else {
            userList=new HashMap<>();
            result=gson.toJson(userList);
        }
        PrintWriter pw = response.getWriter();
        pw.print(result);
        //result的可能值:"" ; Map序列的Json形式
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
