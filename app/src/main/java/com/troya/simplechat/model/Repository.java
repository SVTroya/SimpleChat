package com.troya.simplechat.model;

import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {

    private static ChatMessageDao mChatMessageDao;

    private ExecutorService mExecutorService;

    public Repository(ChatMessageDao chatMessageDao) {
        mChatMessageDao = chatMessageDao;
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ChatMessage>> getAllMessages(String getAllMessages) {
        return mChatMessageDao.getAllMessages(getAllMessages);
    }

    public void addMessage(final ChatMessage message) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mChatMessageDao.addMessage(message);
            }
        });
    }
}
