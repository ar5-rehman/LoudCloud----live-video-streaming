package com.tech.loudcloud.UserPackage.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;
import com.tech.loudcloud.UserPackage.pojoClasses.LiveArtistPojo;
import com.tech.loudcloud.liveVideoPlayer.LiveVideoPlayerActivity;

import java.util.List;

public class LiveArtistAdapter extends RecyclerView.Adapter<LiveArtistAdapter.MyViewHolder>{

    List<LiveArtistPojo> list;
    Context context;
    String userName, userImage;
    UserMainScreenActivity activity;

    public LiveArtistAdapter(List<LiveArtistPojo> list, Context context, String userName, String userImage)
    {
        this.list = list;
        this.context = context;
        activity = (UserMainScreenActivity) context;
        this.userName = userName;
        this.userImage = userImage;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.live_users_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        LiveArtistPojo liveArtistPojo = list.get(position);
        String channelName = liveArtistPojo.getChannelName();
        String artistName = liveArtistPojo.getArtistName();
        String artistId = liveArtistPojo.getArtistId();
        holder.userName.setText(artistName);
        Glide.with(context).load(liveArtistPojo.getArtistImageUrl()).diskCacheStrategy( DiskCacheStrategy.DATA ).into(holder.userImage);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("channelName", channelName);
                Intent in = new Intent(context, LiveVideoPlayerActivity.class);
                in.putExtra("streamName", channelName);
                in.putExtra("artistId", artistId);
                in.putExtra("userName", userName);
                in.putExtra("userImage", userImage);
                context.startActivity(in);
                context.sendBroadcast(new Intent().setAction("com.tech.loudcloud.UserPackage.adapters").putExtras(bundle));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        CardView cardView;
        ImageView userImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }

}
