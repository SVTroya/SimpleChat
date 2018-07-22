package com.troya.simplechat.controllers;

import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.troya.simplechat.R;
import com.troya.simplechat.adapters.ChatAdapter;
import com.troya.simplechat.model.AppDatabase;
import com.troya.simplechat.model.ChatMessage;
import com.troya.simplechat.model.Repository;
import com.troya.simplechat.veiwmodels.ChatViewModel;

import java.util.List;

public class ChatFragment extends Fragment {
    private static final int LAYOUT = R.layout.fragment_chat;

    private static final String KEY_CHAT_NAME = "chat_name";

    private ChatCallback mCallback;

    private EditText mMessageView;
    private AppCompatImageView mSendButton;
    private RecyclerView mChatView;
    private String mChatName;

    private ChatAdapter mAdapter;

    private ChatViewModel mViewModel;

    public ChatFragment() {
    }

    public static ChatFragment newInstance(String chatName) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(KEY_CHAT_NAME, chatName);
        fragment.setArguments(args);
        return fragment;
    }

    private void initViewModel() {
        if (getActivity() != null) {
            final AppDatabase database = Room.databaseBuilder(
                    getActivity().getApplication(),
                    AppDatabase.class,
                    MainActivity.DB_NAME
            ).build();
            Repository repository = new Repository(database.messageDao());
            mViewModel = new ChatViewModel(repository);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChatName = getArguments().getString(KEY_CHAT_NAME);
        }
        initViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMessageView = view.findViewById(R.id.editMessage);
        mSendButton = view.findViewById(R.id.btnSend);
        mChatView = view.findViewById(R.id.recyclerChat);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mMessageView.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    mCallback.onMessageSend(message);
                    mMessageView.setText("");
                }
            }
        });

        mAdapter = new ChatAdapter(null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        mChatView.setLayoutManager(layoutManager);
        mChatView.setAdapter(mAdapter);

        mViewModel.getAllMessages(mChatName).observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(@Nullable List<ChatMessage> chatMessages) {
                mAdapter.setData(chatMessages);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatCallback) {
            mCallback = (ChatCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DeviceSearchCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface ChatCallback {
        void onMessageSend(String message);
    }
}
