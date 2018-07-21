package com.troya.simplechat.model;

import java.io.Serializable;
import java.net.InetAddress;

public class User implements Serializable {

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";

    private String mUserId;
    private String mUserName;
    private int mPort;
    private InetAddress mIPAddress;

    public User() {
    }

    //TODO: check if id needed
    public User(String userId, String userName) {
        mUserId = userId;
        mUserName = userName;
    }

    public User(String userName, InetAddress IPAddress, int port) {
        mUserName = userName;
        mPort = port;
        mIPAddress = IPAddress;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public int getPort() {
        return mPort;
    }

    public InetAddress getIPAddress() {
        return mIPAddress;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public void setIPAddress(InetAddress IPAddress) {
        mIPAddress = IPAddress;
    }
}
