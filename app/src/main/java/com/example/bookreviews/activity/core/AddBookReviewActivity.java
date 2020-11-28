package com.example.bookreviews.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AddBookReviewActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView nameTV, usernameTV, errorTV;
    private EditText bookTitleET, reviewTextET;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private String email, title, reviewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book_review);
        setActionBarTitle("Write a Book Review");
        getInstances();
        findViewsByIds();
        setUserData();
    }
    private void getInstances() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
    }
    private void findViewsByIds(){
        errorTV = findViewById(R.id.errorTV);
        photoIV = findViewById(R.id.photoIV);
        nameTV = findViewById(R.id.nameTV);
        usernameTV = findViewById(R.id.usernameTV);
        bookTitleET = findViewById(R.id.bookTitleTV);
        reviewTextET = findViewById(R.id.reviewTextTV);
    }
    private void setUserData(){
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        getPhoto();
        nameTV.setText(sharedPreferences.getString(StaticClass.NAME, "no name"));
        usernameTV.setText(sharedPreferences.getString(StaticClass.USERNAME, "no name"));
    }
    private void getPhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(email + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap btm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                photoIV.setImageBitmap(Bitmap.createScaledBitmap(btm, photoIV.getWidth(),
                        photoIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed at getting profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_book_review_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_book_review){
            post();
        }
        return super.onOptionsItemSelected(item);
    }
    private void post(){
        if(!validData())
            return;
        DocumentReference reviewReference = database.collection("reviews")
                .document();
        reviewReference.set(reviewMap());
        startActivity(new Intent(getApplicationContext(), CoreActivity.class));
    }
    private boolean validData(){
        title = bookTitleET.getText().toString();
        if(title.isEmpty()){
            displayErrorTV(R.string.unspecified_title);
            return false;
        }
        reviewText = reviewTextET.getText().toString();
        if(reviewText.isEmpty()){
            displayErrorTV(R.string.unspecified_review);
            return false;
        }
        return true;
    }
    private HashMap<String, Object> reviewMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("user", email);
        map.put("title", title);
        map.put("review", reviewText);
        map.put("likes-count", 0);
        map.put("dislikes-count", 0);
        map.put("likes-users", new ArrayList<String>());
        map.put("dislikes-users", new ArrayList<String>());
        map.put("time", System.currentTimeMillis());
        return map;
    }
    private void displayErrorTV(int resourceID) {
        errorTV.setText(resourceID);
        errorTV.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorTV.setVisibility(View.GONE);
            }
        }, 1500);
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
