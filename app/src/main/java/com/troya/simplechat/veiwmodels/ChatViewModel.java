package com.troya.simplechat.veiwmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.troya.simplechat.model.ChatMessage;
import com.troya.simplechat.model.Repository;

import java.util.List;

public class ChatViewModel extends ViewModel {
    private Repository mRepository;

    public ChatViewModel(Repository repository) {
        mRepository = repository;
    }

    public LiveData<List<ChatMessage>> getAllMessages(String getAllMessages) {
        return mRepository.getAllMessages(getAllMessages);
    }
}
