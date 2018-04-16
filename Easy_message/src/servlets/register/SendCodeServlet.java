package servlets.register;

import connection.Conn;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "SendCodeServlet", urlPatterns = "/SendCode")
public class SendCodeServlet extends HttpServlet {
    public static Map<String, String> userverification = new HashMap<>();

    public static void main(String[] args) {
        String verifiCode = tools.Register.createNewCode();
        verifiCode = "%d1%e9%d6%a4%c2%eb%a3%ba" + verifiCode + "%a3%ac%b4%f2%cb%c0%b6%bc%b2%bb%d2%aa%b8%e6%cb%df%b1%f0%c8%cb%c5%b6%a3%a1";
        System.out.println(verifiCode);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String phoneNum = request.getParameter("phone_num");


        Connection connection = Conn.getConnection();
        String sql = "SELECT phoneNum FROM userinfo WHERE phoneNum=?";
        PrintWriter printWriter = response.getWriter();
        try {
            boolean repetition = false;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, phoneNum);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                repetition = true;
                break;
            }
            if (repetition) {
                printWriter.print(false);                     //手机号码重复（只能注册一次）
                System.out.println("\n手机号曾经被用于注册\n");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        String Code = tools.Register.createNewCode();
        String verifiCode = "%d1%e9%d6%a4%c2%eb%a3%ba" + Code + "%a3%ac%b4%f2%cb%c0%b6%bc%b2%bb%d2%aa%b8%e6%cb%df%b1%f0%c8%cb%c5%b6%a3%a1";
        String urlAddress = "http://api02.monyun.cn:7901/sms/v2/std/single_send";
        HttpURLConnection uRLConnection;
        URL url;
        String resultInfo;
        try {
            url = new URL(urlAddress);
            uRLConnection = (HttpURLConnection) url.openConnection();
            uRLConnection.setDoInput(true);
            uRLConnection.setDoOutput(true);
            uRLConnection.setRequestMethod("POST");
            uRLConnection.setUseCaches(false);
            uRLConnection.setInstanceFollowRedirects(true);
            uRLConnection.setRequestProperty("Content-Type", "text/json");
            uRLConnection.connect();

            DataOutputStream out = new DataOutputStream(uRLConnection.getOutputStream());
            String content = "{\"apikey\":\"02034fa17e5b0ba3a5c51f653e60cb6d\",\"mobile\":\"" + phoneNum + "\",\"content\":\"" + verifiCode + "\"}";
            System.out.println(content);
            out.writeBytes(content);
            out.flush();
            out.close();

            InputStream is = uRLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response1 = "";
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                //response = br.readLine();
                response1 = response1 + readLine;
            }
            is.close();
            br.close();
            uRLConnection.disconnect();
            resultInfo = response1;
            String result = resultInfo.split(":")[1].split(",")[0];//0
            System.out.println(response1);
            System.out.println("返回数字:" + result);
            if (!userverification.containsKey(phoneNum)) {
                userverification.put(phoneNum, Code);
                System.out.println("手机号为" + phoneNum + "的用户收到了验证码:" + Code + ".验证码匹配成功！");
            } else if (userverification.containsKey(phoneNum)) {
                userverification.replace(phoneNum, Code);
                System.out.println("手机号为" + phoneNum + "的用户收到了验证码:" + Code + ".验证码更新成功！");
            }
            printWriter.print(true);

            Set<String> set = userverification.keySet();
            Iterator<String> itr = set.iterator();
            while (itr.hasNext()) {
                String key = itr.next();
                String value = userverification.get(key);
                System.out.println("key:" + key + "  value:" + value);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
