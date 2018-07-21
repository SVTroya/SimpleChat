package com.troya.simplechat.veiwmodels;

import android.arch.lifecycle.ViewModel;

import com.troya.simplechat.model.ChatMessage;
import com.troya.simplechat.model.Repository;

public class MainViewModel extends ViewModel {
    private Repository mRepository;

    public MainViewModel(Repository repository) {
        mRepository = repository;
    }

    public void addMessage(ChatMessage message) {
        mRepository.addMessage(message);
    }
}
