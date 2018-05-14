package model.property;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class User {
    public static void main(String[] args){
        byte[] bytes=null;
        Gson gson=new Gson();
        String trans=gson.toJson(bytes);
        System.out.println(trans);
        Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();
        byte[] bytes1=gson1.fromJson(trans,type);
        System.out.println(bytes1);
    }

    private final int propertyNum=10;
    private String userID;
    private String passWord=null;
    private String nickName;
    /**
     * 头像
     **/
    private byte[] headIcon=null;
    private boolean isMale=false;
    private String email=null;
    private String phoneNum=null;
    private String exitTime=null;
    private String birthday=null;
    private String intro=null;

    /**
     * 生日用TimeStamp还是String?
     **/

    public User(String userID, String nickName, boolean isMale, String birthday, String email, String phoneNum, String exitTime) {
        this.userID = userID;
        this.nickName = nickName;
        this.isMale = isMale;
        this.birthday = birthday;
        this.email = email;
        this.phoneNum = phoneNum;
        this.exitTime = exitTime;
    }

    public User(String userID, String nickName, byte[] headIcon, boolean isMale, String email, String phoneNum, String exitTime, String birthday) {
        this.userID = userID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.isMale = isMale;
        this.email = email;
        this.phoneNum = phoneNum;
        this.exitTime = exitTime;
        this.birthday = birthday;
    }

    public User(String userID, String nickName, byte[] headIcon, boolean isMale, String email, String phoneNum, String exitTime, String birthday, String intro) {
        this.userID = userID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.isMale = isMale;
        this.email = email;
        this.phoneNum = phoneNum;
        this.exitTime = exitTime;
        this.birthday = birthday;
        this.intro = intro;
    }

    public User(String userID, String passWord, String nickName, byte[] headIcon, boolean isMale, String email, String phoneNum, String birthday, String intro) {
        this.userID = userID;
        this.passWord = passWord;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.isMale = isMale;
        this.email = email;
        this.phoneNum = phoneNum;
        this.birthday = birthday;
        this.intro = intro;
    }

    public User(String userID, String nickName, String intro) {
        this.userID = userID;
        this.nickName = nickName;
        this.intro = intro;
    }

    public User(String userID, String nickName, byte[] headIcon, String intro) {
        this.userID = userID;
        this.nickName = nickName;
        this.headIcon = headIcon;
        this.intro = intro;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public byte[] getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(byte[] headIcon) {
        this.headIcon = headIcon;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * 地址需不需要?
     **/

    public int getPropertyNum() {
        return propertyNum;
    }
}
