package com.example.bookreviews.activity.core.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.activity.core.CreateBookReviewActivity;
import com.example.bookreviews.adapter.SearchBookAdapter;
import com.example.bookreviews.adapter.SearchUserAdapter;
import com.example.bookreviews.model.Book;
import com.example.bookreviews.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;


public class SearchFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private FloatingActionButton addBookReviewFAB;
    private EditText searchET;
    private TextView noResultsTV, booksTV, usersTV;
    private RecyclerView booksRV, usersRV;
    private SearchBookAdapter searchBookAdapter;
    private ArrayList<Book> booksList = new ArrayList<>();
    private SearchUserAdapter searchUserAdapter;
    private ArrayList<User> usersList = new ArrayList<>();
    private FirebaseFirestore database;
    private String email;
    private boolean booksUsers = false; // false=>books | true=>users

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_search, container, false);
        context = fragmentView.getContext();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();
        getInstances();
        findViewsByIds();
        setListeners();
        setRecyclerViews();
        return fragmentView;
    }
    private void getInstances(){
        email = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        database = FirebaseFirestore.getInstance();
    }
    private void findViewsByIds(){
        searchET = fragmentView.findViewById(R.id.searchET);
        noResultsTV = fragmentView.findViewById(R.id.noResultsTV);
        booksRV = fragmentView.findViewById(R.id.booksRV);
        usersRV = fragmentView.findViewById(R.id.usersRV);
        addBookReviewFAB = fragmentView.findViewById(R.id.addBookFAB);
        booksTV = fragmentView.findViewById(R.id.booksTV);
        usersTV = fragmentView.findViewById(R.id.usersTV);
    }
    private void setListeners(){
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noResultsTV.setVisibility(View.GONE);
                booksList.clear();
                searchBookAdapter.notifyDataSetChanged();
                usersList.clear();
                searchUserAdapter.notifyDataSetChanged();
                if(s.toString().length()!=0) search(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addBookReviewFAB.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(context, CreateBookReviewActivity.class)); }});
        booksTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchTab(false);
            }
        });
        usersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchTab(true);
            }
        });
    }
    private void toggleSearchTab(boolean tab){
        booksUsers = tab;
        booksTV.setBackground(!tab ?
                context.getDrawable(R.drawable.special_background_left_tab_active) :
                context.getDrawable(R.drawable.special_background_left_tab_nonactive));
        booksTV.setTextColor(!tab ?
                context.getColor(R.color.white) :
                context.getColor(R.color.special));
        usersTV.setBackground(tab ?
                context.getDrawable(R.drawable.special_background_right_tab_active) :
                context.getDrawable(R.drawable.special_background_right_tab_nonactive));
        usersTV.setTextColor(tab ?
                context.getColor(R.color.white) :
                context.getColor(R.color.special));
        booksRV.setVisibility(!tab ? View.VISIBLE : View.GONE);
        usersRV.setVisibility(tab ? View.VISIBLE : View.GONE);
        booksList.clear();
        searchBookAdapter.notifyDataSetChanged();
        usersList.clear();
        searchUserAdapter.notifyDataSetChanged();
        if(tab) searchUser(searchET.getText().toString());
        else    searchBook(searchET.getText().toString());
    }
    private void setRecyclerViews(){
        searchBookAdapter = new SearchBookAdapter(context, booksList);
        booksRV.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        booksRV.setAdapter(searchBookAdapter);
        searchUserAdapter = new SearchUserAdapter(context, usersList);
        usersRV.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        usersRV.setAdapter(searchUserAdapter);
    }
    private void search(String queryText){
        if(!booksUsers){ // search books
            searchBook(queryText);
        }else{ // search users
            searchUser(queryText);
        }
    }
    private void searchBook(String queryText){
        noResultsTV.setVisibility(View.GONE);
        database.collection("books")
                .whereGreaterThanOrEqualTo("title", queryText.toUpperCase())
                .whereLessThanOrEqualTo("title", queryText.toLowerCase()+"\uF8FF")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                getBookFromDocument(document);
                            }
                        }else{
                            noResultsTV.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("INDEX", e.getMessage());
                        Toast.makeText(context, "Failed at getting results", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void getBookFromDocument(DocumentSnapshot document){
        Book book = new Book();
        book.setId(document.getId());
        book.setTitle(String.valueOf(document.get("title")));
        book.setReviewsNumber((long) document.get("reviews-number"));
        booksList.add(book);
        searchBookAdapter.notifyDataSetChanged();
    }
    private void searchUser(String queryText){
        noResultsTV.setVisibility(View.GONE);
        database.collection("users")
                .whereGreaterThanOrEqualTo("name", queryText.toUpperCase())
                .whereLessThanOrEqualTo("name", queryText.toLowerCase()+"\uF8FF")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                getUserFromDocument(document);
                            }
                        }else{
                            noResultsTV.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("INDEX", e.getMessage());
                        Toast.makeText(context, "Failed at getting results", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void getUserFromDocument(DocumentSnapshot document){
        if(document.getId().equals(email)) return;
        User user = new User();
        user.setId(document.getId());
        user.setUsername(String.valueOf(document.get("username")));
        user.setName(String.valueOf(document.get("name")));
        usersList.add(user);
        searchUserAdapter.notifyDataSetChanged();
    }
}
