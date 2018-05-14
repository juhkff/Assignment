package model.contact;

public class FullContact {
    private String nickName;        //昵称
    private String motto=null;           //个性签名
    private byte[] pic=null;             //头像数组
    private int state;              //上下线
    private String sort=null;            //分组
    private String id;
    private String username;        //备注

    public FullContact(String nickName, byte[] pic, int state, String sort, String id, String username) {
        this.nickName = nickName;
        this.pic = pic;
        this.state = state;
        this.sort = sort;
        this.id = id;
        this.username = username;
    }

    public FullContact(String nickName, String motto, byte[] pic, int state, String sort, String id, String username) {
        this.nickName = nickName;
        this.motto = motto;
        this.pic = pic;
        this.state = state;
        this.sort = sort;
        this.id = id;
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
