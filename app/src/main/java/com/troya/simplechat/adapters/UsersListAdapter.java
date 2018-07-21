package com.troya.simplechat.adapters;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.troya.simplechat.R;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UserViewHolder> {

    private static final int LAYOUT = R.layout.user;

    private List<NsdServiceInfo> mData;
    private int mSelectedPosition = -1;
    private Context mContext;
    private Callback mCallback;

    public UsersListAdapter(List<NsdServiceInfo> data, Callback callback) {
        mData = data;
        mCallback = callback;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new UserViewHolder(layoutInflater.inflate(LAYOUT, parent, false));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setup(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView mUserNameView;
        AppCompatImageView mCellphoneImageView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mUserNameView = itemView.findViewById(R.id.txtUserName);
            mCellphoneImageView = itemView.findViewById(R.id.imgCellphone);
        }

        void setup(NsdServiceInfo serviceInfo) {
            String serviceName = serviceInfo.getServiceName()
                    .substring(0, serviceInfo.getServiceName().indexOf("//"));
            mUserNameView.setText(serviceName);
            mUserNameView.setTextColor(ContextCompat.getColor(mContext, R.color.textColor));
            mCellphoneImageView.setImageResource(R.drawable.ic_cellphone);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserNameView.setTextColor(ContextCompat.getColor(mContext, R.color.textColorLight));
                    mCellphoneImageView.setImageResource(R.drawable.ic_cellphone_selected);

                    if (mSelectedPosition > -1) {

                        notifyItemChanged(mSelectedPosition);
                    }

                    if (mCallback != null) {
                        mCallback.onUserSelected(mData.get(getAdapterPosition()));
                    }

                    mSelectedPosition = getAdapterPosition();
                }
            });
        }
    }

    public interface Callback {
        void onUserSelected(NsdServiceInfo nsdServiceInfo);
    }
}
