
package com.tech.loudcloud.UserPackage.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.UserMainScreenActivity;
import com.tech.loudcloud.UserPackage.pojoClasses.CampaignsPojo;
import com.tech.loudcloud.UserPackage.userFragments.VoteCastFragment;

import java.util.ArrayList;
import java.util.List;

public class CampaignsAdapter extends RecyclerView.Adapter<CampaignsAdapter.MyViewHolder> implements Filterable {

    List<CampaignsPojo> list;

    List<CampaignsPojo> filterList;

    Context context;
    UserMainScreenActivity activity;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    double total_votes;

    public CampaignsAdapter(Context context, List<CampaignsPojo> list){
        this.context = context;
        this.list = list;
        activity = (UserMainScreenActivity) context;

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        filterList = new ArrayList<>(list);

        getUserVotes();
    }

    @NonNull
    @Override
    public CampaignsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.campaigns_layout, parent, false);
        CampaignsAdapter.MyViewHolder myViewHolder = new CampaignsAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignsAdapter.MyViewHolder holder, int position) {
        CampaignsPojo campaignsPojo = list.get(position);

        Glide.with(context).load(campaignsPojo.getArtistPic()).diskCacheStrategy( DiskCacheStrategy.DATA ).into(holder.artistPic);
        holder.artistName.setText(campaignsPojo.getArtistName());
        holder.location.setText(campaignsPojo.getLocation());
        holder.songsList.setText(campaignsPojo.getListOfSongs());
        holder.alternativeSongs.setText(campaignsPojo.getAlternativeSongs());
        holder.alternativeLocation.setText(campaignsPojo.getAlternativeLocation());
        holder.neededVotes.setText(campaignsPojo.getVotesNeeded());
        holder.earnedVotes.setText(campaignsPojo.getVotesEarned());

        holder.castBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(total_votes==0.0)
                {
                    new AlertDialog.Builder(context)
                            .setTitle("OOPS!")
                            .setMessage("Please buy votes first!")

                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })

                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }else {

                        FragmentManager manager = activity.getSupportFragmentManager();
                        VoteCastFragment mydialog = new VoteCastFragment(context);

                        Bundle bundle = new Bundle();

                        bundle.putString("artistName", campaignsPojo.getArtistName());
                        bundle.putString("location", campaignsPojo.getLocation());
                        bundle.putString("alternativeLocation", campaignsPojo.getAlternativeLocation());
                        bundle.putString("performance", campaignsPojo.getListOfSongs());
                        bundle.putString("alternativePerformance", campaignsPojo.getAlternativeSongs());

                        mydialog.setArguments(bundle);
                        mydialog.show(manager, "votecast");

                }


            }
        });
    }

    public void getUserVotes()
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e !=null)
                {

                }
                else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if(documentChange.getDocument().exists()) {
                            String userEmail = documentChange.getDocument().getData().get("Email").toString();
                            String votes = documentChange.getDocument().getData().get("Votes").toString();

                            if(user.getEmail().equals(userEmail)) {
                                total_votes = Double.valueOf(votes);

                            }
                        }
                    }
                }
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
            List<CampaignsPojo> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CampaignsPojo item : filterList) {
                    if (item.getArtistName().toLowerCase().contains(filterPattern)) {
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

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView artistPic;
        TextView artistName, location, songsList, alternativeSongs, neededVotes, earnedVotes, alternativeLocation;
        Button castBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            artistPic = itemView.findViewById(R.id.artistPic);
            artistName = itemView.findViewById(R.id.artistName);
            location = itemView.findViewById(R.id.location);
            songsList = itemView.findViewById(R.id.songsList);
            alternativeSongs = itemView.findViewById(R.id.alternativeSongs);
            neededVotes = itemView.findViewById(R.id.votesNeeded);
            earnedVotes = itemView.findViewById(R.id.votesEarned);
            alternativeLocation = itemView.findViewById(R.id.alternativeLocations);
            castBtn = itemView.findViewById(R.id.castVoteBtn);
        }
    }
}