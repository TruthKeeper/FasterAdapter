package com.tk.sampleadapter;

/**
 * <pre>
 *      author : TK
 *      time : 2017/7/15
 *      desc :
 * </pre>
 */

public class User {
    private String nickname;
    private int id;
    private boolean vip;
    private boolean star;
    private String desc;

    public User(String nickname, int id, boolean vip, boolean star, String desc) {
        this.nickname = nickname;
        this.id = id;
        this.vip = vip;
        this.star = star;
        this.desc = desc;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
