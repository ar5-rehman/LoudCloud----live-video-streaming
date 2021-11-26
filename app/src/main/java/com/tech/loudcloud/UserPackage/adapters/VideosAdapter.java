package com.tech.loudcloud.UserPackage.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;
import com.tech.loudcloud.UserPackage.VideoPlayerActivity;
import com.tech.loudcloud.UserPackage.pojoClasses.CampaignsPojo;
import com.tech.loudcloud.UserPackage.pojoClasses.VideosLinksPojo;

import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyViewHolder> implements Filterable {

    List<VideosLinksPojo> list;
    List<VideosLinksPojo> filterList;
    Context context;
    UserMainScreenActivity activity;

    public VideosAdapter(List<VideosLinksPojo> list, Context context)
    {
        this.list = list;
        this.context = context;
        activity = (UserMainScreenActivity) context;
        filterList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.videos_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        VideosLinksPojo videosLinksPojo = list.get(position);

        String link = videosLinksPojo.getLink();

        Glide.with(context).load(link).into(holder.thumbnail);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playerIntent = new Intent(context, VideoPlayerActivity.class);
                playerIntent.putExtra("link", link);
                context.startActivity(playerIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<VideosLinksPojo> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (VideosLinksPojo item : filterList) {
                    if (item.getStreamName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        ImageView thumbnail;
       // VideoView videoView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            //videoView = itemView.findViewById(R.id.video);
        }
    }

}

