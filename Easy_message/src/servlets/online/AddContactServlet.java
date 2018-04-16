package servlets.online;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

@WebServlet(name = "AddContactServlet",urlPatterns = "/AddContact")
public class AddContactServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        Map<String,String> userList = null;
        try {
            userList=Online.getAddList(userID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String result="";
        if(userList.size()>0) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            result = gson.toJson(userList);
        }
        PrintWriter pw=response.getWriter();
        pw.print(result);
        //result的可能值:"" ; Map序列的Json形式
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
