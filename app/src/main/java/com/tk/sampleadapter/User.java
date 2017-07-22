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
    private int type;

    public User(String nickname, int id, int type) {
        this.nickname = nickname;
        this.id = id;
        this.type = type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (type != user.type) return false;
        return nickname != null ? nickname.equals(user.nickname) : user.nickname == null;

    }

    @Override
    public int hashCode() {
        int result = nickname != null ? nickname.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + type;
        return result;
    }
}
