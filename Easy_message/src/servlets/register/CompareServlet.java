package servlets.register;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

@WebServlet(name = "CompareServlet", urlPatterns = "/Compare")
public class CompareServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String phoneNum = request.getParameter("phone_num");
        String verificationCode = request.getParameter("code");
        PrintWriter printWriter = response.getWriter();
        String Code = null;

        try {
            Code = SendCodeServlet.userverification.get(phoneNum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (verificationCode.equals(Code) && verificationCode != null) {
            printWriter.print(true);
            System.out.println("\n手机号" + phoneNum + "注册验证成功！");
            SendCodeServlet.userverification.remove(phoneNum);
        } else {
            System.out.println("\n手机号" + phoneNum + "注册验证失败...");
            printWriter.print(false);
        }
        Set<String> result = SendCodeServlet.userverification.keySet();
        Iterator<String> iterator = result.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = SendCodeServlet.userverification.get(key);
            System.out.println("key:" + key + "  value:" + value);
        }
        System.out.println();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
