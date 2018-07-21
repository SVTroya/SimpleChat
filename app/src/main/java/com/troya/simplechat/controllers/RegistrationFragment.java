package com.troya.simplechat.controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.troya.simplechat.R;

public class RegistrationFragment extends Fragment {

    private static final int LAYOUT = R.layout.fragment_registration;

    private RegistrationCallback mCallback;

    private EditText mUserNameView;
    private Button mOkButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUserNameView = view.findViewById(R.id.editUserName);
        mOkButton = view.findViewById(R.id.btnOk);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onNewUserAdded(mUserNameView.getText().toString());
                }
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegistrationCallback) {
            mCallback = (RegistrationCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RegistrationFragment.DeviceSearchCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface RegistrationCallback {
        void onNewUserAdded(String userName);
    }
}
