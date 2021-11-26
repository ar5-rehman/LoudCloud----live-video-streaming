package com.tech.loudcloud.UserPackage.userFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.tech.loudcloud.R;
import com.tech.loudcloud.UserPackage.pojoClasses.VideosLinksPojo;
import com.tech.loudcloud.UserPackage.ArchivedVideosService.CountVideosPojo;
import com.tech.loudcloud.UserPackage.ArchivedVideosService.VideosPojo;
import com.tech.loudcloud.UserPackage.ArchivedVideosService.WebServices;
import com.tech.loudcloud.UserPackage.adapters.VideosAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchievedStreamFragment extends Fragment implements BillingProcessor.IBillingHandler{

    WebServices myWebService;
    private VideosAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    ArrayList<VideosLinksPojo> list;
    Context context;

    private BillingProcessor bp;
    private TextView tvStatus;
    private Button btnPremium;
    private TransactionDetails purchaseTransactionDetails = null;

    public ArchievedStreamFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archieved_stream, container, false);

        getActivity().setTitle("The Loud Cloud");

        setHasOptionsMenu(true);

        bp = new BillingProcessor(context, getResources().getString(R.string.play_console_license), this);
        bp.initialize();

        tvStatus = view.findViewById(R.id.tv_status);
        btnPremium = view.findViewById(R.id.btn_premium);

        list = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.videosRecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    private void getStreams(){
        myWebService = WebServices.retrofit.create(WebServices.class);
        Call<CountVideosPojo> call = myWebService.getCount();
        call.enqueue(new Callback<CountVideosPojo>() {
            @Override
            public void onResponse(Call<CountVideosPojo> call, Response<CountVideosPojo> response) {

                if(response.isSuccessful())
                {
                    int totalRounds = 50;
                    CountVideosPojo count  = response.body();
                    int totalVideos = count.getNumber();
                    float total = totalVideos/50f;
                    int totalScreens = (int) Math.ceil(total);

                    if(totalVideos <= totalRounds) {
                        totalRounds = totalVideos;
                    }

                    for(int j = 0; j < totalScreens; j++) {

                        Call<List<VideosPojo>> videoCall = myWebService.getVideosList(j, 50);

                        int finalTotalRounds = totalRounds;
                        videoCall.enqueue(new Callback<List<VideosPojo>>() {
                            @Override
                            public void onResponse(Call<List<VideosPojo>> call, Response<List<VideosPojo>> response) {

                                if (response.isSuccessful()) {
                                    List<VideosPojo> getData = response.body();

                                    for (int i = 0; i < finalTotalRounds; i++) {
                                        String streamName = getData.get(i).getStreamName();
                                        String link = "http://3.236.184.191:5080/LiveApp/streams/" + streamName;

                                        list.add(new VideosLinksPojo(link, streamName));
                                        adapter = new VideosAdapter(list, context);
                                        recyclerView.setAdapter(adapter);
                                    }

                                }
                            }

                            @Override
                            public void onFailure(Call<List<VideosPojo>> call, Throwable t) {
                                Toast.makeText(context, "V Fail" + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        });

                        int remVideos;
                        remVideos = totalVideos-50;
                        totalVideos = remVideos;
                        if(remVideos<50){
                            totalRounds = remVideos;
                        }else{
                            totalRounds = 50;
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<CountVideosPojo> call, Throwable t) {
                Toast.makeText(context,"Fail", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private boolean hasSubscription() {
        if (purchaseTransactionDetails != null) {
            return purchaseTransactionDetails.purchaseInfo != null;
        }
        return false;
    }

    @Override
    public void onBillingInitialized() {

        Log.d("MainActivity", "onBillingInitialized: ");

        String premium = getResources().getString(R.string.premium);

        purchaseTransactionDetails = bp.getSubscriptionTransactionDetails(premium);

        bp.loadOwnedPurchasesFromGoogle();

        btnPremium.setOnClickListener(v -> {
            if (bp.isSubscriptionUpdateSupported()) {
                bp.subscribe(getActivity(), premium);
            } else {
                Log.d("MainActivity", "onBillingInitialized: Subscription updated is not supported");
            }
        });

        if (hasSubscription()) {
            tvStatus.setText("Status: Premium");
            getStreams();
        } else {
            tvStatus.setText("Status: Free");
            btnPremium.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.d("MainActivity", "onProductPurchased: ");
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d("MainActivity", "onPurchaseHistoryRestored: ");

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d("MainActivity", "onBillingError: ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.campaign_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_settings);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}