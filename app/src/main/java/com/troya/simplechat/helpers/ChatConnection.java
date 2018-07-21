package com.troya.simplechat.helpers;

import android.util.Log;

import com.troya.simplechat.model.ChatMessage;
import com.troya.simplechat.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChatConnection {
    private static final String TAG = ChatConnection.class.getSimpleName();

    private ChatServer mChatServer;
    private ChatClient mChatClient;
    private Socket mSocket;
    private int mPort = -1;
    private User mSelectedUser;
    private User mOwner;
    private RSACipher mCipher;
    private ConnectionCallback mCallback;
    private PublicKey mSelectedUserPublicKey;

    public ChatConnection(ConnectionCallback callback) {
        mCallback = callback;
        mChatServer = new ChatServer();
        try {
            mCipher = new RSACipher();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    public void setOwner(User owner) {
        mOwner = owner;
    }

    public void tearDown() {
        mChatServer.tearDown();
        if (mChatClient != null) {
            mChatClient.tearDown();
        }
    }

    public void connect(User user) {
        mSelectedUser = user;
        connectToServer(user.getIPAddress(), user.getPort());
    }

    public void connectToServer(InetAddress ipAddress, int port) {
        if (mChatClient == null) {
            mChatClient = new ChatClient(ipAddress, port);
        }
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg, true);
        }
    }

    public void sendPublicKey() {
        if (mChatClient != null) {

            String publicKey = "-----BEGIN PUBLIC KEY-----" +
                    mCipher.publicKeyToString() +
                    "-----END PUBLIC KEY-----";
            mChatClient.sendMessage(publicKey, false);
        }
    }

    public void sendUserId() {
        if (mChatClient != null) {

            String userId = "-----BEGIN USER ID-----" +
                    mCipher.publicKeyToString() +
                    "-----END USER ID-----";
            mChatClient.sendMessage(userId, false);
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }

    public synchronized void saveMessages(String msg, User user, byte messageType) {
        Log.e(TAG, "Updating chat: " + msg);

        ChatMessage message = new ChatMessage(msg, messageType, user.getUserId());
        message.setTime(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime()));
        mCallback.onMessageSave(message);
    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        private static final String SERVER_TAG = "ServerClient";

        ServerSocket mServerSocket = null;
        Thread mThread;

        public ChatServer() {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ex) {
                Log.e(TAG, "Error while closing server socket.");
            }
        }

        class ServerThread implements Runnable {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Connected.");
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            User connectedUser = new User();
                            connectedUser.setIPAddress(address);
                            connectedUser.setPort(port);
                            connect(connectedUser);
                        }
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Error creating ServerSocket: ", ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {
        private static final String CLIENT_TAG = "ChatClient";

        private InetAddress mAddress;
        private int mPort;
        private Thread mSendThread;
        private Thread mReceiveThread;

        public ChatClient(InetAddress address, int port) {
            Log.d(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.mPort = port;
            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {
            private static final int QUEUE_CAPACITY = 10;

            BlockingQueue<String> mMessageQueue;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, mPort));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. Skipping!");
                    }
                    mReceiveThread = new Thread(new ReceivingThread());
                    mReceiveThread.start();
                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }

                sendPublicKey();
                sendUserId();
                mCallback.onConnection(mSelectedUser);

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg, true);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "ChatMessage sending loop interrupted, exiting");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {
            @Override
            public void run() {
                BufferedReader input;

                try {
                    input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                    while (!Thread.currentThread().isInterrupted()) {
                        String msg;
                        msg = input.readLine();
                        if (msg != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + msg);
                            if (msg.contains("-----BEGIN PUBLIC KEY-----") && msg.contains("-----END PUBLIC KEY-----")) {
                                msg = msg.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
                                mSelectedUserPublicKey = RSACipher.stringToPublicKey(msg);
                            } else if (msg.contains("-----BEGIN USER ID-----") && msg.contains("-----END USER ID-----")) {
                                mSelectedUser.setUserId(msg.replace("-----BEGIN USER ID-----", "").replace("-----END USER ID-----", ""));
                            } else {
                                String decryptedMessage = mCipher.decrypt(msg);
                                saveMessages(decryptedMessage, mSelectedUser, ChatMessage.SENT_MESSAGE_TYPE);
                            }
                        }
                        else {
                            Log.d(CLIENT_TAG, "Message is null!!");
                            break;
                        }
                    }
                    input.close();
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                } catch (NoSuchPaddingException | IllegalBlockSizeException
                        | BadPaddingException | InvalidKeyException
                        | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }

        public void tearDown() {
            try {
                getSocket().close();
            } catch (IOException ex) {
                Log.e(CLIENT_TAG, "Error when closing server socket.", ex);
            }
        }

        public void sendMessage(String msg, boolean shouldEncode) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null!!!");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null!!!");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                String preparedMessage = msg;
                if (mSelectedUserPublicKey != null && shouldEncode) {
                    preparedMessage = mCipher.encrypt(preparedMessage,
                            mSelectedUserPublicKey);
                }

                out.println(preparedMessage);
                out.flush();
                if (shouldEncode) {
                    saveMessages(msg, mOwner, ChatMessage.RECEIVED_MESSAGE_TYPE);
                }
            } catch (UnknownHostException ex) {
                Log.d(CLIENT_TAG, "Unknown Host", ex);
            } catch (IOException ex) {
                Log.d(CLIENT_TAG, "I/O Exception", ex);
            } catch (Exception ex) {
                Log.d(CLIENT_TAG, "Unexpected error:", ex);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + msg);
        }
    }

    public interface ConnectionCallback {
        void onConnection(User user);

        void onMessageSave(ChatMessage message);
    }
}
