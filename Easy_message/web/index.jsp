<%--
  Created by IntelliJ IDEA.
  User: dell
  Date: 3/21/2018
  Time: 6:14 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>$Title$</title>
</head>
<body>
<form action="/Easy_message/Register" method="post">
    <table>
        <tr>
            <td>请输入你的昵称：</td>
            <td><input type="text" placeholder="请输入昵称" name="nickName"></td>
        </tr>
        <tr>
            <td>请输入你的密码：</td>
            <td><input type="password" placeholder="请输入密码" name="passWord"></td>
        </tr>
        <tr>
            <td>请输入你的邮箱：</td>
            <td><input type="email" placeholder="请输入正确的邮箱格式" name="email"></td>
        </tr>

        <tr>
            <td><input type="submit" placeholder="注册" value="点击注册"></td>
        </tr>
    </table>
</form>

<form action="/Easy_message/Login" method="post">
    <table>
        <tr>
            <td>用户名：</td>
            <td><input type="number" name="userID" placeholder="请输入用户名"></td>
        </tr>
        <tr>
            <td>密码：</td>
            <td><input type="password" name="passWord" placeholder="请输入密码"></td>
        </tr>
        <tr>
            <td><input type="submit" value="登录"></td>
        </tr>
    </table>
</form>

<form action="/Easy_message/Upload" method="post" enctype="multipart/form-data">
    <table>
        <tr>
            <td>请输入帐号：</td>
            <td><input type="text" name="userID"></td>
            <td>选择要上传的文件：</td>
            <td><input type="file" name="upload"></td>
            <td><input type="submit" value="上传"></td>
        </tr>
    </table>
</form>

<form action="/Easy_message/SendCode" method="post">
    <table>
        <tr>
            <td>手机号:</td>
            <td><input type="number" name="phone_num"></td>
            <td><input type="submit" value="提交"></td>
        </tr>
    </table>
</form>

<form action="/Compare" method="post">
    <table>
        <tr>
            <td>手机号:</td>
            <td><input type="number" name="phone_num"></td>
        </tr>
        <tr>
            <td>验证码:</td>
            <td><input type="text" name="code"></td>
        </tr>
        <tr>
            <td><input type="submit" value="提交"></td>
        </tr>
    </table>
</form>

<form action="http://api02.monyun.cn:7901/sms/v2/std/single_send" method="post">
    <input type="text" name="content">
    <input type="submit" value="提交">
</form>
</body>
</html>
