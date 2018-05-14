package servlets.offline;

import tools.Exit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "ExitServlet", urlPatterns = "/Exit")
public class ExitServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        int result2 = 0;
        try {
            Exit.changeStatus(userID);                          //更新好友列表中自己的状态   (status: 1--->0 )
            Exit.changeGroupStatus(userID);                     //更新群列表中自己的状态
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            result2 = Exit.updateExitTime(userID);                        //更新下线时间
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter pw = response.getWriter();
        if (result2 != 1) {
            pw.print("error");
        } else {
            pw.print("exit");                                           //返回的可能值:error/exit
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
