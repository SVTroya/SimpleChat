package com.troya.simplechat.model;

import java.io.Serializable;
import java.net.InetAddress;

public class User implements Serializable {
    private String mUserId;
    private String mUserName;
    private int mPort;
    private InetAddress mIPAddress;

    public User() {
    }

    public User(String userId, String userName) {
        mUserId = userId;
        mUserName = userName;
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
