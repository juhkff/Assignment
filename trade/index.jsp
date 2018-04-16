<%--
  Created by IntelliJ IDEA.
  User: dell
  Date: 2017/12/8
  Time: 22:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>$Title$</title>
  </head>
  <body>
  <form action="/trade/LoginUser" method="post">
    <table>
      <tr><td>用户名：</td><td><input type="text" name="username"></td></tr>
      <tr><td>密码：</td><td><input type="text" name="password"></td></tr>
      <tr><td><input type="submit" align="center" value="登录"></td></tr>
    </table>
  </form>
  </body>
</html>
