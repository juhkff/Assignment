package servlets;

import com.google.gson.Gson;
import connection.Conn;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

@WebServlet(name = "TestServlet")
public class TestServlet extends HttpServlet {
    public static void main(String[] args){
        /*ArrayList<String> result=new ArrayList<String>();
        result.add("123");
        result.add("456");
        Gson gson=new Gson();
        String trans=gson.toJson(result);
        System.out.println(trans);*/
        char i='☻';
        System.out.println(i);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID=request.getParameter("userID");
        ArrayList<String> result=new ArrayList<String>();
        result.add("123");
        result.add("456");
        Gson gson=new Gson();
        String trans=gson.toJson(result);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
