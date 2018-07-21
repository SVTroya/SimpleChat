package com.troya.simplechat.controllers;

import android.arch.persistence.room.Room;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.troya.simplechat.R;
import com.troya.simplechat.helpers.ChatConnection;
import com.troya.simplechat.helpers.NsdHelper;
import com.troya.simplechat.helpers.SharedPrefManager;
import com.troya.simplechat.model.AppDatabase;
import com.troya.simplechat.model.ChatMessage;
import com.troya.simplechat.model.Repository;
import com.troya.simplechat.model.User;
import com.troya.simplechat.veiwmodels.MainViewModel;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements DeviceSearchDialog.DeviceSearchCallback,
        RegistrationFragment.RegistrationCallback, MainFragment.Callback, ChatFragment.ChatCallback,
        ChatConnection.ConnectionCallback, NsdHelper.NSDCallback {

    private static final int LAYOUT = R.layout.activity_main;
    public static final String TAG = "NsdChat";
    public static final String DB_NAME = "SimpleChat.db";

    private NsdHelper mNsdHelper;
    private ChatConnection mConnection;
    private MainViewModel mViewModel;
    FragmentManager mFragmentManager;
    private User mOwner;

    private User getSavedOwner() {
        return SharedPrefManager.getUserInfo(this);
    }

    private void initViewModel() {
        final AppDatabase database = Room.databaseBuilder(
                getApplication(),
                AppDatabase.class,
                DB_NAME
        ).build();
        Repository repository = new Repository(database.messageDao());
        mViewModel = new MainViewModel(repository);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        mOwner = getSavedOwner();
        initViewModel();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        mConnection = new ChatConnection(this);
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFragmentManager = getSupportFragmentManager();

        if (mOwner == null) {
            setRegistrationFragment();
        } else {
            mNsdHelper.setServiceName(mOwner.getUserName());
            mConnection.setOwner(mOwner);
            setMainFragment();
            startAdvertise();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (mNsdHelper != null) {
            mNsdHelper.tearDown();
        }
        super.onPause();
    }

    public void startAdvertise() {
        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort(), mOwner);
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    private DeviceSearchDialog showDeviceSearchDialog() {
        FragmentManager fm = getSupportFragmentManager();
        DeviceSearchDialog dialog = DeviceSearchDialog.newInstance();
        dialog.show(fm, DeviceSearchDialog.TAG);
        return dialog;
    }

    public void setChatFragment(String chatName) {
        ChatFragment fragment = ChatFragment.newInstance(chatName);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragment, fragment).commit();
    }

    private void setRegistrationFragment() {
        RegistrationFragment fragment = new RegistrationFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment, fragment).commit();
    }

    private void setMainFragment() {
        MainFragment fragment = new MainFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment, fragment).commit();
    }

    @Override
    public void onNewUserAdded(String userName) {
        SharedPrefManager.saveUserInfo(this, new User(UUID.randomUUID().toString(), userName));
        recreate();
    }

    @Override
    public void onSearchClick() {
        DeviceSearchDialog dialog = showDeviceSearchDialog();
        mNsdHelper.discoverServices(dialog);
    }

    @Override
    public void onDialogDismissListener() {
        mNsdHelper.stopDiscovery();
    }

    @Override
    public void onChoiceConfirm(NsdServiceInfo serviceInfo) {
        if (serviceInfo != null) {
            Log.d(TAG, "Connecting.");
            mNsdHelper.resolveService(serviceInfo);
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    @Override
    public void onMessageSend(String message) {
        mConnection.sendMessage(message);
    }

    @Override
    public void onConnection(User user) {
        setChatFragment(user.getUserId());
    }

    @Override
    public void onMessageSave(ChatMessage message) {
        mViewModel.addMessage(message);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        User user = new User();
        String userId = serviceInfo.getServiceName().substring(serviceInfo.getServiceName().indexOf("//") + 2);
        user.setUserId(userId);
        user.setIPAddress(serviceInfo.getHost());
        user.setPort(serviceInfo.getPort());
        mConnection.connect(user);
        mConnection.sendPublicKey();
    }
}
