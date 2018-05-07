package servlets.group;

import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "UpdateGroupInfoServlet", urlPatterns = "/UpdateGroupInfo")
public class UpdateGroupInfoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        String groupID = request.getParameter("groupID");
        PrintWriter printWriter = response.getWriter();
        try {
            Group.updateGroupInfo(userID,groupID);
            printWriter.print("success");
        }catch (Exception e){
            System.out.println("error in UpdateGroupInfoServlet !");
            printWriter.print("error in UpdateGroupInfoServlet");
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
