package com.example.bookreviews.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.adapter.MyReviewsAdapter;
import com.example.bookreviews.model.Review;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MyReviewsActivity extends AppCompatActivity {

    private TextView noReviewsTV;
    private RecyclerView reviewsRV;
    private MyReviewsAdapter adapter;
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private FirebaseFirestore database;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);
        setActionBarTitle("My Book Reviews");
        getInstances();
        findViewsByIds();
        setRV();
        getMyReviews();
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE)
                .getString(StaticClass.EMAIL, "no email");
    }
    private void findViewsByIds(){
        noReviewsTV = findViewById(R.id.noReviewsTV);
        reviewsRV = findViewById(R.id.reviewsRV);
    }
    private void setRV(){
        adapter = new MyReviewsAdapter(getApplicationContext(), reviewsList);
        reviewsRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                RecyclerView.VERTICAL, false));
        reviewsRV.setAdapter(adapter);
    }
    private void getMyReviews() {
        database.collection("reviews")
                .whereEqualTo("user", email)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                                getReviewFromDocument(document);
                            }
                        }else{
                            noReviewsTV.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("INDEX", e.getMessage());
                    }
                });
    }
    private void getReviewFromDocument(DocumentSnapshot document){
        Review review = new Review();
        review.setId(document.getId());
        review.setBook(String.valueOf(document.get("title")));
        review.setReviewText(String.valueOf(document.get("review")));
        review.setTime((long)document.get("time"));
        review.setLikesCount((long)document.get("likes-count"));
        review.setDislikesCount((long)document.get("dislikes-count"));
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
        startActivity(new Intent(getApplicationContext(), CoreActivity.class)
        .putExtra(StaticClass.TO, StaticClass.PROFILE_FRAGMENT));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
