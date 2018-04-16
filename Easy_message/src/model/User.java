package model;

public class User {
    private String userID;
    private String nickName;
    /**头像**/
    private boolean isMale;
    private String email;
    private String phoneNum;
    private String exitTime;
    private String birthday;                        /**生日用TimeStamp还是String?**/

    public User(String userID, String nickName, boolean isMale, String birthday, String email, String phoneNum, String exitTime) {
        this.userID = userID;
        this.nickName = nickName;
        this.isMale = isMale;
        this.birthday = birthday;
        this.email = email;
        this.phoneNum = phoneNum;
        this.exitTime = exitTime;
    }
    /**地址需不需要?**/



    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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
}
