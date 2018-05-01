package tools;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class Email {
    public static void main(String[] args) {
        String mail = "2860533946@qq.com";
        try {
            send(mail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static final String localAddress = "2912897177@qq.com";        //本地邮件地址
    public static final String passWord = "aqko251068";                     //邮箱密码
    //public static final String localAddress = "Easy_message@163.com";
    //public static final String passWord="Easymessage163";

    public static final String verificationCode = "njvplbxrzyivdgfg";       //授权码

    public static void send(String emailAddress) throws MessagingException {
       /*Properties prop = new Properties();
        prop.setProperty("mail.transport.protocol", "smtp");//定义邮件发送协议
        prop.setProperty("mail.smtp.host", "smtp.163.com");//声明邮件服务器地址
        prop.setProperty("mail.smtp.auth", "true");//发送权限，为true时表示允许发送
        prop.setProperty("mail.debug", "true");//设置为true时，调试的时候可以在控制台显示信息


        Session session = Session.getInstance(prop);//相当于建立了一条通信路线
        ChatMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(localAddress));//发件者邮箱
        msg.setRecipient(RecipientType.TO, new InternetAddress(emailAddress));//收件邮箱
        msg.setSubject("来自Easy_message：新用户注册验证");
        msg.setText("您的验证码为："+Register.createNewCode()+" 请在一分钟内使用该验证码注册\\n\\n如果您没有注册，请忽略这封邮件\\n\\n\\n版权所有©Easy_message");

        Transport tran = session.getTransport();
        tran.connect(localAddress.split("@")[0], passWord);//假设q号为982690136的密码为123456
        tran.sendMessage(msg,msg.getAllRecipients());*/


        // 创建Properties 类用于记录邮箱的一些属性
        Properties props = new Properties();
        // 表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.auth", "true");
        //此处填写SMTP服务器
        props.put("mail.smtp.host", "smtp.qq.com");
        //端口号，QQ邮箱给出了两个端口，但是另一个我一直使用不了，所以就给出这一个587
        props.put("mail.smtp.port", "587");
        // 此处填写你的账号
        props.put("mail.user", localAddress);
        // 此处的密码就是前面说的16位STMP口令
        props.put("mail.password", verificationCode);

        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(
                props.getProperty("mail.user"));
        message.setFrom(form);

        // 设置收件人的邮箱
        InternetAddress to = new InternetAddress(emailAddress);
        message.setRecipient(RecipientType.TO, to);

        // 设置邮件标题
        message.setSubject("来自Easy_message：新用户注册验证");

        // 设置邮件的内容体
        message.setContent("您的验证码为：" + Register.createNewCode() + "\n请在一分钟内使用该验证码注册\n\n如果您没有注册，请忽略这封邮件\n\n\n版权所有©Easy_message", "text/html;charset=UTF-8");

        // 最后当然就是发送邮件啦
        Transport.send(message);

    }

}
