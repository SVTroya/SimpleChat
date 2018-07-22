package com.troya.simplechat.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.troya.simplechat.R;
import com.troya.simplechat.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private static final int RECEIVED_MESSAGE_LAYOUT = R.layout.received_message;
    private static final int SENT_MESSAGE_LAYOUT = R.layout.sent_message;

    private List<ChatMessage> mData;
    private RecyclerView mRecyclerView;

    public ChatAdapter(List<ChatMessage> data) {
        mData = data;
    }

    public void setData(List<ChatMessage> data) {
        mData = data;
        notifyDataSetChanged();
        mRecyclerView.scrollToPosition(mData.size() - 1);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageViewHolder viewHolder;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == ChatMessage.RECEIVED_MESSAGE_TYPE) {
            viewHolder = new MessageViewHolder(layoutInflater.inflate(RECEIVED_MESSAGE_LAYOUT, parent, false));
        }
        else  {
            viewHolder = new MessageViewHolder(layoutInflater.inflate(SENT_MESSAGE_LAYOUT, parent, false));
        }

        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.setup(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return (mData != null) ? mData.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getMessageType();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        TextView messageTimeView;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.txtMessage);
            messageTimeView = itemView.findViewById(R.id.txtTime);
        }

        void setup(ChatMessage message) {
            messageTextView.setText(message.getMessage());
            messageTimeView.setText(message.getTime());
        }
    }
}
