package com.troya.simplechat.helpers;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.troya.simplechat.controllers.DeviceSearchDialog;
import com.troya.simplechat.controllers.MainActivity;
import com.troya.simplechat.model.User;

import java.io.Serializable;

public class NsdHelper implements Serializable {
    public static final String TAG = NsdHelper.class.getSimpleName();
    public static final String SERVICE_TYPE = "_http._tcp.";

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.ResolveListener mResolveListener;
    private String mServiceName;
    private NSDCallback mCallback;

    private DeviceSearchDialog mDialog = null;

    public void initializeNsd() {
        initializeResolveListener();
    }

    public void setServiceName(String serviceName) {
        mServiceName = serviceName;
    }

    public NsdHelper(MainActivity activity) {
        mCallback = activity;
        mNsdManager = (NsdManager) activity.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else {
                    mDialog.getFoundServices().add(service);
                    if (mDialog.getActivity() != null) {
                        mDialog.getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mDialog.getListAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);

                for (NsdServiceInfo serviceInfo : mDialog.getFoundServices()) {
                    if (serviceInfo.getServiceName().equals(service.getServiceName())) {
                        mDialog.getFoundServices().remove(serviceInfo);
                    }
                }

                if (mDialog.getActivity() != null) {
                    mDialog.getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mDialog.getListAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered: " + mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "Service registration failed: " + arg1);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Service unregistration failed: " + errorCode);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mCallback.onServiceResolved(serviceInfo);
            }
        };
    }

    public void registerService(int port, User owner) {
        tearDown();  // Cancel any previous registration request
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName + "//" + owner.getUserId());
        serviceInfo.setServiceType(SERVICE_TYPE);
        /*serviceInfo.setAttribute(User.KEY_USER_ID, owner.getUserId());
        serviceInfo.setAttribute(User.KEY_USER_NAME, owner.getUserName());*/
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void resolveService(NsdServiceInfo serviceInfo) {
        mNsdManager.resolveService(serviceInfo, mResolveListener);
    }

    public void discoverServices(DeviceSearchDialog dialog) {
        stopDiscovery();  // Cancel any existing discovery request
        mDialog = dialog;
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mDiscoveryListener = null;
        }
    }

    public void tearDown() {
        if (mRegistrationListener != null) {
            try {
                mNsdManager.unregisterService(mRegistrationListener);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mRegistrationListener = null;
        }
    }

    public interface NSDCallback {
        void onServiceResolved(NsdServiceInfo serviceInfo);
    }
}
