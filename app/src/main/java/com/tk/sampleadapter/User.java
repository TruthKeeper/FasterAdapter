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
    private boolean gender;
    private int id;

    public User(String nickname, boolean gender, int id) {
        this.nickname = nickname;
        this.gender = gender;
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id == user.id;
    }
}
