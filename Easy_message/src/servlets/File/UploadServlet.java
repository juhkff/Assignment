package servlets.File;

import tools.Chat;
import tools.Online;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "UploadServlet",urlPatterns = "/Upload")
public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        String anotherID=request.getParameter("anotherID");
        String fileName=request.getParameter("fileName");
        String sendTime=request.getParameter("sendTime");

        /**用户单纯的离线上传是不需要这一步的**/
        tools.file.File.upLoadNewFile(userID, anotherID ,fileName,sendTime,"");
//        Chat.updateContactStatus(userID, anotherID);

        PrintWriter printWriter=response.getWriter();
        printWriter.print("success");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
