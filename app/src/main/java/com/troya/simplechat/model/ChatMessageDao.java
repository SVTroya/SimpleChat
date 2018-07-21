package com.troya.simplechat.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM messages WHERE chat_name = :chatName")
    LiveData<List<ChatMessage>> getAllMessages(String chatName);

    @Insert
    long addMessage(ChatMessage category);
}
