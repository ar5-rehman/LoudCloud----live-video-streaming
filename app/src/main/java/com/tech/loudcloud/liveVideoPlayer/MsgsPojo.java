package com.tech.loudcloud.liveVideoPlayer;

public class MsgsPojo {

    public MsgsPojo(String msg, String userName,String userImage){
        this.userMessage = msg;
        this.userName = userName;
        this.userImage = userImage;
    }

    public String getMsg() {
        return userMessage;
    }

    public void setMsg(String msg) {
        this.userMessage = msg;
    }

    String userMessage;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    String userName;
    String userImage;
}
