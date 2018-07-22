package com.troya.simplechat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "messages")
public class ChatMessage {

    public static final byte SENT_MESSAGE_TYPE = 0;
    public static final byte RECEIVED_MESSAGE_TYPE = 1;


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long mId;

    @ColumnInfo(name = "message")
    @NonNull
    private String mMessage;

    @ColumnInfo(name = "message_type")
    private int mMessageType;

    @ColumnInfo(name = "message_time")
    private String mTime;

    @ColumnInfo(name = "user_name")
    private String mUserName;

    @ColumnInfo(name = "chat_name")
    private String mChatName;

    public ChatMessage() {
    }

    @Ignore
    public ChatMessage(@NonNull String message, int messageType, @NonNull String chatName) {
        mMessage = message;
        mMessageType = messageType;
        mChatName = chatName;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    @NonNull
    public String getMessage() {
        return mMessage;
    }

    public void setMessage(@NonNull String message) {
        mMessage = message;
    }

    public int getMessageType() {
        return mMessageType;
    }

    public void setMessageType(int messageType) {
        mMessageType = messageType;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getChatName() {
        return mChatName;
    }

    public void setChatName(String chatName) {
        mChatName = chatName;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }
}
