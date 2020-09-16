package com.example.uploaddata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ViewData extends AppCompatActivity {

    RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private RecyclerView.Adapter recyclerAdapter;
    String Database_Path = "All_Image_Uploads_Database";
    private ArrayList<ImageUploadInfo> uploads =new ArrayList<>();
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);
        spinner=findViewById(R.id.filterType);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(ViewData.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        databaseReference= FirebaseDatabase.getInstance().getReference(Database_Path);

        String spinnerValue=spinner.getSelectedItem().toString();
        
        //Get Data From FireBase
        getDataFromFireBase();
    }
    private void getDataFromFireBase(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clearAll();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ImageUploadInfo imageUploadInfo = snapshot.getValue(ImageUploadInfo.class);
                    uploads.add(imageUploadInfo);
                }
                recyclerAdapter=new RecyclerAdapter(getApplicationContext(),uploads);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void clearAll(){
        if(uploads!=null){
            uploads.clear();
        }
        uploads=new ArrayList<>();
    }
}