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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (vip != user.vip) return false;
        if (star != user.star) return false;
        if (nickname != null ? !nickname.equals(user.nickname) : user.nickname != null)
            return false;
        return desc != null ? desc.equals(user.desc) : user.desc == null;
    }

    @Override
    public int hashCode() {
        int result = nickname != null ? nickname.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + (vip ? 1 : 0);
        result = 31 * result + (star ? 1 : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        return result;
    }
}
