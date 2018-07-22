package com.troya.simplechat.controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.troya.simplechat.R;
import com.troya.simplechat.adapters.UsersListAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceSearchDialog extends DialogFragment implements View.OnClickListener, UsersListAdapter.Callback {

    public static final String TAG = DeviceSearchDialog.class.getSimpleName();
    private static final int LAYOUT = R.layout.find_device_dialog;

    private DeviceSearchCallback mCallback;
    private List<NsdServiceInfo> mFoundServices = new ArrayList<>();
    private NsdServiceInfo mSelectedService;
    private UsersListAdapter mListAdapter;

    public static DeviceSearchDialog newInstance() {
        return new DeviceSearchDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return inflater.inflate(LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setTitle(R.string.device_search_dialog_title);

        Button okButton = view.findViewById(R.id.btnOk);
        Button cancelButton = view.findViewById(R.id.btnCancel);
        RecyclerView userListView = view.findViewById(R.id.recyclerDevices);

        mListAdapter = new UsersListAdapter(mFoundServices, this);
        userListView.setLayoutManager(new LinearLayoutManager(getContext()));
        userListView.setAdapter(mListAdapter);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mCallback.onDialogDismissListener();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DeviceSearchCallback) {
            mCallback = (DeviceSearchCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DeviceSearchDialog.DeviceSearchCallback");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOk:
                if (mCallback != null) {
                    if (mSelectedService != null) {

                        mCallback.onChoiceConfirm(mSelectedService);
                        dismiss();
                    }
                    else {
                        Toast.makeText(getContext(), "Please choose user!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.btnCancel:
                dismiss();
                break;
        }
    }

    @Override
    public void onUserSelected(NsdServiceInfo serviceInfo) {
        mSelectedService = serviceInfo;
    }

    public List<NsdServiceInfo> getFoundServices() {
        return mFoundServices;
    }

    public UsersListAdapter getListAdapter() {
        return mListAdapter;
    }

    public interface DeviceSearchCallback {
        void onChoiceConfirm(NsdServiceInfo serviceInfo);

        void onDialogDismissListener();
    }
}
