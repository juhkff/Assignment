<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>$Test$</title>
  </head>
  <body>
  <%--<%request.getAttribute("sss");%>--%>
  <form action="/dieGo/AddUser" method="post">
    <table>
        <tr>
            <td>手机号码：</td><td><input type="text" name="phoneNum"></td>
        </tr>
        <tr>
            <td>密码</td><td><input type="password" name="password"></td>
        </tr>
        <tr>
            <td colspan="2" align="center"><input type="submit"  value="注册"></td>
        </tr>
    </table>
  </form>

  <form action="/dieGo/SearchUser" method="post">
      <table>
          <tr>
              <td>输入你要查询的用户userId：</td><input type="text" name="userId"><td>
          </tr>
          <tr>
              <td><input type="submit" value="查询用户信息"></td>
          </tr>
      </table>
  </form>

  <form action="/dieGo/UserLogin" method="post">
      <table>
          <tr>
              <td>登录界面</td>
          </tr>
          <tr>
              <td>请输入手机号：</td><td><input type="text" name="phoneNum"></td>
          </tr>
          <tr>
              <td>请输入密码：</td><td><input type="password" name="password"></td>
          </tr>
          <tr>
              <td colspan="2"><input type="submit" value="登录"></td>
          </tr>
      </table>
  </form>

  <form action="/dieGo/UserUpdate" method="post">
      <table>
          <tr>
              <td colspan="2">更改用户信息</td>
          </tr>
          <tr>
              <td>用户userId：</td><td><input type="number" max="9999999" min="1000000" name="userId"></td>
          </tr>
          <tr>
              <td>用户昵称：</td><td><input type="text" name="nickName"></td>
          </tr>
          <tr>
              <td>密码：</td><td><input type="password" name="password"></td>
          </tr>
          <tr>
              <td>电话号码：</td><td><input type="number" name="phoneNum"></td>
          </tr>
          <tr>
              <td>电话区号：</td><td><input type="number" name="disCode"></td>
          </tr>
          <tr>
              <td>用户头像字符串：</td><td><input type="text" name="headIcon"></td>
          </tr>
          <tr>
              <td>校区代码（一位整数）：</td><td><input type="number"maxlength="1" name="campusCode"></td>
          </tr>
          <tr>
              <td>身份证号（18位）：</td><td><input type="text" maxlength="18" minlength="18" name="IDCode"></td>
          </tr>
          <tr>
              <td>真实姓名：</td><td><input type="text" name="realName"></td>
          </tr>
          <tr>
              <td>学号：</td><td><input type="number" name="campusIdCode"></td>
          </tr>
          <tr>
              <td>默认地址：</td><td><input type="text" name="defaultAddress"></td>
          </tr>
          <tr>
              <td>是否实名认证：</td><td><input type="text" name="isVerified"></td>
          </tr>
          <tr>
              <td colspan="2" align="center"></td><td><input type="submit" value="提交修改"></td>
          </tr>
      </table>
  </form>
  <form action="/dieGo/SearchOrder" method="post">
      <table>
          <tr><td>输入要查询所有订单的用户userId：</td><td><input type="number" name="userId"></td></tr>
          <tr><td colspan="2" align="center"></td><td><input type="submit" value="查询"></td></tr>
      </table>
  </form>

  <%--需要发单人填写：订单类型；联系方式；校区代码；订单公开内容；订单私密内容；感谢金额；订单递送时间--%>
  <form action="/dieGo/PublishOrder" method="post">
      <table>
          <tr><td>输入发单人userId：</td><td><input type="number" name="userId"></td></tr>
          <tr><td>输入订单类型：</td><td><input type="number" max="5" min="1" name="orderType"></td></tr>
          <tr><td>输入联系方式：</td><td><input type="text" name="contactWay"></td></tr>
          <tr><td>输入订单公开内容：</td><td><input type="text" name="publicDetails"></td></tr>
          <tr><td>输入订单私密内容：</td><td><input type="text" name="privateDetails"></td></tr>
          <tr><td>输入校区代码：</td><td><input type="number" min="1" max="8" name="campusCode"></td></tr>
          <tr><td>输入感谢金额：</td><td><input type="number" min="1" max="100" name="thankMoney"></td></tr>
          <tr><td>输入订单递送时间：</td><td><input type="number" name="deliveryTime"></td></tr>
          <tr><td colspan="1"></td><td><input type="submit" value="发布订单"></td></tr>
      </table>
  </form>
  </body>
</html>