package servlets.File;

import tools.file.File;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "GetFileAddressServlet",urlPatterns = "/getFileAddress")
public class GetFileAddressServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String senderAddr=request.getParameter("senderAddr");
        String receiverAddr=request.getParameter("receiverAddr");
        String req=senderAddr+"/"+receiverAddr;
        String result="error";
        try {
            result=File.getFileAddress(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter=response.getWriter();
        printWriter.print(result);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
