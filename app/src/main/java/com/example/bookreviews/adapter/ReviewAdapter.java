package com.example.bookreviews.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.activity.core.ProfileActivity;
import com.example.bookreviews.model.Review;
import com.example.bookreviews.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviewsList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private FirebaseFirestore database;
    private FirebaseStorage storage;

    public ReviewAdapter(Context context, List<Review> data) {
        this.mInflater = LayoutInflater.from(context);
        this.reviewsList = data;
        this.context = context;
        this.database = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.review_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Review review = reviewsList.get(position);
        setUserPhoto(holder, review.getReviewerID());
        holder.nameTV.setText(review.getReviewerName());
        holder.usernameTV.setText(review.getReviewerUsername());
        holder.timeTV.setText(castTime(review.getTime()));
        holder.reviewTV.setText(review.getReviewText());
        holder.likeTV.setText(String.valueOf(review.getLikes()));
        holder.dislikeTV.setText(String.valueOf(review.getDislikes()));
        holder.reviewerLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ProfileActivity.class)
                        .putExtra(StaticClass.PROFILE_ID, review.getReviewerID()));
            }
        });
        holder.likeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                like(holder, review);
            }
        });
    }
    private void setUserPhoto(final ViewHolder holder, String reviewerID){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(reviewerID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap btm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.photoIV.setImageBitmap(Bitmap.createScaledBitmap(btm,
                        holder.photoIV.getWidth(), holder.photoIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure at downloading profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    @SuppressLint("SimpleDateFormat")
    private String castTime(long time){
        return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(time));
    }
    private void like(ViewHolder holder, Review review){
        database.collection("reviews")
                .document(review.getId())
                .update("likes", FieldValue.increment(1));
    }
    private void dislike(ViewHolder holder, Review review){
        database.collection("reviews")
                .document(review.getId())
                .update("dislikes", FieldValue.increment(-1));
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout reviewerLL;
        private ImageView photoIV, likeIV, dislikeIV;
        private TextView nameTV, usernameTV, timeTV, reviewTV, likeTV, dislikeTV;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        private void findViewsByIds(){
            reviewerLL = itemView.findViewById(R.id.reviewerLL);
            photoIV = itemView.findViewById(R.id.photoIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            reviewTV = itemView.findViewById(R.id.reviewTV);
            likeIV = itemView.findViewById(R.id.likeIV);
            dislikeIV = itemView.findViewById(R.id.dislikeIV);
            likeTV = itemView.findViewById(R.id.likeTV);
            dislikeTV = itemView.findViewById(R.id.dislikeTV);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    Review getItem(int id) {
        return reviewsList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
