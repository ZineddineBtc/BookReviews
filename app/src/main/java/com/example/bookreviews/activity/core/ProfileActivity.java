package com.example.bookreviews.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.adapter.ProfileReviewsAdapter;
import com.example.bookreviews.model.Review;
import com.example.bookreviews.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView nameTV, usernameTV, bioTV;
    private RecyclerView reviewsRV;
    private ProfileReviewsAdapter adapter;
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String profileID, name, username, bio;
    private Bitmap profilePhotoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getInstances();
        findViewsByIds();
        getProfilePhoto();
    }
    private void getInstances(){
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        nameTV = findViewById(R.id.nameTV);
        usernameTV = findViewById(R.id.usernameTV);
        bioTV = findViewById(R.id.bioTV);
        reviewsRV = findViewById(R.id.booksRV);
    }
    private void getProfilePhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(profileID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToProfilePhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed at getting profile photo", Toast.LENGTH_LONG).show();
                setProfileData();
            }
        });
    }
    private void setBytesToProfilePhoto(byte[] bytes){
        profilePhotoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(profilePhotoBitmap, photoIV.getWidth(),
                photoIV.getHeight(), false));
        setProfileData();
    }
    private void setProfileData(){
        database.collection("users")
                .document(profileID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            name = String.valueOf(document.get("name"));
                            nameTV.setText(name);
                            setActionBarTitle(name);
                            username = String.valueOf(document.get("username"));
                            usernameTV.setText(username);
                            bio = String.valueOf(document.get("bio"));
                            bioTV.setText(bio);
                            setReviewsRV();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at setting profile date", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void setReviewsRV(){
        adapter = new ProfileReviewsAdapter(getApplicationContext(), reviewsList,
                profilePhotoBitmap,
                new User(name, username, bio));
        reviewsRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        reviewsRV.setAdapter(adapter);
        getReviews();
    }
    private void getReviews(){
        database.collection("reviews")
                .whereEqualTo("reviewer-id", profileID)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                            addReviewToList(document);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed at getting profile books", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void addReviewToList(DocumentSnapshot document){
        Review review = new Review();
        review.setId(document.getId());
        review.setBook(String.valueOf(document.get("book")));
        review.setReviewText(String.valueOf(document.get("review")));
        review.setTime((Long) document.get("time"));
        reviewsList.add(review);
        adapter.notifyDataSetChanged();
    }
    public void setActionBarTitle(String title){
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(
                Html.fromHtml("<font color=\"#ffffff\"> "+title+" </font>")
        );
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), CoreActivity.class));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
