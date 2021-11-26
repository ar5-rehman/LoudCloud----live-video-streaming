package com.tech.loudcloud.liveVideoPlayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tech.loudcloud.R;

import java.util.ArrayList;

public class ChatRoom extends BottomSheetDialogFragment {

    EditText msgBox;
    ImageView send;

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    ArrayList<MsgsPojo> list;

    FirebaseDatabase database;
    DatabaseReference mDatabaseRef;
    Context context;
    String artistId;
    String userName, userImage;

    public ChatRoom(Context context, String artistId, String userName, String userImage){
        this.context = context;
        this.artistId = artistId;
        this.userName = userName;
        this.userImage = userImage;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.bottomsheet_layout,
                container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.msg_recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        msgBox = v.findViewById(R.id.msg_box);
        send = v.findViewById(R.id.send);

        database = FirebaseDatabase.getInstance();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = msgBox.getText().toString();

                mDatabaseRef = database.getReference().child(artistId).child("ChatRoom").push();

                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dsp) {
                        mDatabaseRef.child("userMessage").setValue(msg);
                        mDatabaseRef.child("userName").setValue(userName);
                        mDatabaseRef.child("userImage").setValue(userImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                msgBox.setText("");
            }
        });

        getMsgs();

        return v;
    }

    public void getMsgs(){

        mDatabaseRef = database.getReference().child(artistId).child("ChatRoom");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    if(dsp.child("userMessage").exists() && dsp.child("userName").exists() && dsp.child("userImage").exists()) {
                        String msg = dsp.child("userMessage").getValue().toString();
                        String name = dsp.child("userName").getValue().toString();
                        String image = dsp.child("userImage").getValue().toString();


                        list.add(new MsgsPojo(msg, name, image));
                        adapter = new MsgsAdapter(list, context);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
