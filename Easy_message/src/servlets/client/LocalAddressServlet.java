package servlets.client;

import tools.Online;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "LocalAddressServlet",urlPatterns = "/findMyLocalIP")
public class LocalAddressServlet extends HttpServlet {
    public static void main(String[] args){
        String userID="4965757872";
        try {
            String address=Online.getLocalAddress(userID);
            String localIP=address.split(":")[0];
            System.out.println(localIP);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        try {
            String address=Online.getLocalAddress(userID);
            String localIP=address.split(":")[0];
            PrintWriter pw=response.getWriter();
            pw.print(localIP);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
