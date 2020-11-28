package com.example.bookreviews.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.adapter.ReviewAdapter;
import com.example.bookreviews.model.Review;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BookReviewsActivity extends AppCompatActivity {

    private TextView titleTV, reviewsNumberTV;
    private RecyclerView reviewsRV;
    private ReviewAdapter adapter;
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private FirebaseFirestore database;
    private String bookID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reviews);
        getInstances();
        findViewsByIds();
        setBookInfo();
        setRecyclerView();
        getReviews();
    }
    private void getInstances(){
        bookID = getIntent().getStringExtra(StaticClass.BOOK_ID);
        database = FirebaseFirestore.getInstance();
    }
    private void findViewsByIds(){
        titleTV = findViewById(R.id.titleTV);
        reviewsNumberTV = findViewById(R.id.reviewsNumberTV);
        reviewsRV = findViewById(R.id.reviewsRV);
    }
    private void setBookInfo(){
        String title = getIntent().getStringExtra(StaticClass.BOOK_TITLE);
        if(title != null) titleTV.setText(title);
        long reviewsNumber = getIntent().getLongExtra(StaticClass.BOOK_REVIEWS_NUMBER, 0);
        if(title != null){
            String reviewsNumberString;
            if(reviewsNumber>1) {
                reviewsNumberString = reviewsNumber + " reviews";
            }else{
                reviewsNumberString = reviewsNumber + " review";
            }
            titleTV.setText(reviewsNumberString);
        }
    }
    private void setRecyclerView(){
        adapter = new ReviewAdapter(getApplicationContext(), reviewsList);
        reviewsRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        reviewsRV.setAdapter(adapter);
    }
    private void getReviews(){
        database.collection("reviews")
                .whereEqualTo("book", bookID)
                .orderBy("likes", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(DocumentSnapshot document: queryDocumentSnapshots) {
                                setReviewFromDocument(document);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failer at getting reviews", Toast.LENGTH_LONG).show();
                        Log.i("INDEX", e.getMessage());
                    }
                });
    }
    private void setReviewFromDocument(DocumentSnapshot document){
        Review review = new Review();
        review.setId(document.getId());
        review.setReviewerID(String.valueOf(document.get("reviewer-id")));
        review.setReviewerUsername(String.valueOf(document.get("reviewer-username")));
        review.setReviewerName(String.valueOf(document.get("reviewer-name")));
        review.setTime((long) document.get("time"));
        review.setLikesCount((long) document.get("likes"));
        review.setDislikesCount((long) document.get("dislikes"));
        review.setReviewText(String.valueOf(document.get("review-text")));
        reviewsList.add(review);
        adapter.notifyDataSetChanged();
    }
}










