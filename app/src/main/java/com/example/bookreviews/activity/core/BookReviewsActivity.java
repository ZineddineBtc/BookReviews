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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.adapter.ReviewAdapter;
import com.example.bookreviews.model.Review;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BookReviewsActivity extends AppCompatActivity {

    private LinearLayout addReviewLL;
    private EditText myReviewET;
    private TextView titleTV, reviewsNumberTV;
    private RecyclerView reviewsRV;
    private ReviewAdapter adapter;
    private ArrayList<Review> reviewsList = new ArrayList<>();
    private FirebaseFirestore database;
    private String bookID, title, email, name, username, reviewText;
    private long reviewsNumber;
    private Review newReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reviews);
        setActionBarTitle("Reviews");
        getInstances();
        findViewsByIds();
        setBookInfo();
        setRecyclerView();
        getReviews();
    }
    private void getInstances(){
        bookID = getIntent().getStringExtra(StaticClass.BOOK_ID);
        Toast.makeText(getApplicationContext(), bookID, Toast.LENGTH_LONG).show();
        database = FirebaseFirestore.getInstance();
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        name = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.NAME, "no name");
        username = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.USERNAME, "no username");
        title = getIntent().getStringExtra(StaticClass.BOOK_TITLE);
        reviewsNumber = getIntent().getLongExtra(StaticClass.BOOK_REVIEWS_NUMBER, 0);
    }
    private void findViewsByIds(){
        titleTV = findViewById(R.id.titleTV);
        reviewsNumberTV = findViewById(R.id.reviewsNumberTV);
        reviewsRV = findViewById(R.id.reviewsRV);
        FloatingActionButton addReview = findViewById(R.id.addBookFAB);
        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { addReviewLL.setVisibility(View.VISIBLE);
            }
        });
        addReviewLL = findViewById(R.id.addReviewLL);
        myReviewET = findViewById(R.id.myReviewET);
    }
    private void setBookInfo(){
        titleTV.setText(title);
        if(title != null) titleTV.setText(title);

        if(title != null){
            String reviewsNumberString;
            if(reviewsNumber>1) {
                reviewsNumberString = reviewsNumber + " reviews";
            }else{
                reviewsNumberString = reviewsNumber + " review";
            }
            reviewsNumberTV.setText(reviewsNumberString);
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
                .whereEqualTo("book-id", bookID)
                .orderBy("likes", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(DocumentSnapshot document: queryDocumentSnapshots) {
                                setReviewFromDocument(document);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at getting reviews", Toast.LENGTH_LONG).show();
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
        Toast.makeText(getApplicationContext(), String.valueOf(reviewsList.size()) , Toast.LENGTH_LONG).show();
    }
    public void addReview(View view){
        reviewText = myReviewET.getText().toString();
        if(!reviewText.isEmpty()){
            database.collection("reviews").document().set(reviewMap());
            database.collection("books").document(bookID)
                    .update("reviews-number", FieldValue.increment(1));
            reviewsNumber++;
            setBookInfo();
            reviewsList.add(newReview);
            adapter.notifyDataSetChanged();
        }
    }
    private HashMap<String, Object> reviewMap(){
        setNewReview();
        HashMap<String, Object> map = new HashMap<>();
        map.put("reviewer-id", email);
        map.put("reviewer-name", name);
        map.put("reviewer-username", username);
        map.put("title", title);
        map.put("review", reviewText);
        map.put("likes-count", 0);
        map.put("dislikes-count", 0);
        map.put("likes-users", new ArrayList<String>());
        map.put("dislikes-users", new ArrayList<String>());
        map.put("book-id", bookID);
        map.put("time", newReview.getTime());
        return map;
    }
    private void setNewReview(){
        newReview.setReviewerID(email);
        newReview.setReviewerName(name);
        newReview.setReviewerUsername(username);
        newReview.setBook(title);
        newReview.setReviewText(reviewText);
        newReview.setLikesCount(0);
        newReview.setDislikesCount(0);
        newReview.setLikesUsers(new ArrayList<String>());
        newReview.setDislikesUsers(new ArrayList<String>());
        newReview.setBookID(bookID);
        newReview.setTime(System.currentTimeMillis());
    }
    public void cancel(View view){
        addReviewLL.setVisibility(View.GONE);
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
        if(addReviewLL.getVisibility()==View.VISIBLE){
            addReviewLL.setVisibility(View.GONE);
        }else{
            startActivity(new Intent(getApplicationContext(), CoreActivity.class));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}










