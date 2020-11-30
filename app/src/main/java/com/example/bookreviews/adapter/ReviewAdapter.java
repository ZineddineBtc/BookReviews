package com.example.bookreviews.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences sharedPreferences;

    public ReviewAdapter(Context context, List<Review> data) {
        this.mInflater = LayoutInflater.from(context);
        this.reviewsList = data;
        this.context = context;
        this.database = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
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
        setTextualData(holder, review);
        setLikesCount(holder, review);
        setDislikesCount(holder, review);
        setLikedOrDisliked(holder, review);
        setListeners(holder, review);
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
    private void setTextualData(ViewHolder holder, Review review){
        holder.nameTV.setText(review.getReviewerName());
        holder.usernameTV.setText(review.getReviewerUsername());
        holder.timeTV.setText(castTime(review.getTime()));
        holder.reviewTV.setText(review.getReviewText());
    }
    @SuppressLint("SimpleDateFormat")
    private String castTime(long time){
        return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(time));
    }
    private void setLikedOrDisliked(ViewHolder holder, Review review){
        if(review.getLikesUsers()
                .contains(sharedPreferences.getString(StaticClass.EMAIL, " "))){
            holder.likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_special));
            holder.liked = true;
        }else if(review.getDislikesUsers()
                .contains(sharedPreferences.getString(StaticClass.EMAIL, " "))){
            holder.dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_special));
            holder.disliked = true;
        }
    }
    private void setLikesCount(ViewHolder holder, Review review){
        int likesCount = (int) review.getLikesCount();
        StringBuilder likesText = new StringBuilder();
        if(likesCount>1000 && likesCount<1000000){
            likesCount = likesCount/1000;
            likesText.append(likesCount).append("K");
        }else if(likesCount>1000000){
            likesCount = likesCount/1000000;
            likesText.append(likesCount).append("M");
        }else{
            likesText.append(likesCount);
        }
        holder.likesCountTV.setText(likesText);
    }
    private void setDislikesCount(ViewHolder holder, Review review){
        int dislikesCount = (int) review.getDislikesCount();
        StringBuilder dislikesText = new StringBuilder();
        if(dislikesCount>1000 && dislikesCount<1000000){
            dislikesCount = dislikesCount/1000;
            dislikesText.append(dislikesCount).append("K");
        }else if(dislikesCount>1000000){
            dislikesCount = dislikesCount/1000000;
            dislikesText.append(dislikesCount).append("M");
        }else{
            dislikesText.append(dislikesCount);
        }
        holder.dislikesCountTV.setText(dislikesText);
    }
    private void likeOnClickListener(ViewHolder holder, Review review){
        if(holder.liked){
            database.collection("reviews")
                    .document(review.getId())
                    .update("likes-users", FieldValue.arrayRemove(
                            sharedPreferences.getString(StaticClass.EMAIL, " ")));
            database.collection("reviews")
                    .document(review.getId())
                    .update("likes-count", FieldValue.increment(-1));
            holder.likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_grey));
            review.setLikesCount(review.getLikesCount()-1);
            holder.liked = false;
        }else{
            if(holder.disliked){
                dislikeOnClickListener(holder, review);
            }
            database.collection("reviews")
                    .document(review.getId())
                    .update("likes-users", FieldValue.arrayUnion(
                            sharedPreferences.getString(StaticClass.EMAIL, " ")));
            database.collection("reviews")
                    .document(review.getId())
                    .update("likes-count", FieldValue.increment(1));
            holder.likesIV.setImageDrawable(context.getDrawable(R.drawable.ic_like_special));
            review.setLikesCount(review.getLikesCount()+1);
            holder.liked = true;
        }
        setLikesCount(holder, review);
    }
    private void dislikeOnClickListener(ViewHolder holder, Review review){
        if(holder.disliked){
            database.collection("reviews")
                    .document(review.getId())
                    .update("dislikes-users", FieldValue.arrayRemove(
                            sharedPreferences.getString(StaticClass.EMAIL, " ")));
            database.collection("reviews")
                    .document(review.getId())
                    .update("dislikes-count", FieldValue.increment(-1));
            holder.dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_grey));
            holder.dislikesCountTV.setText(
                    String.valueOf(review.getDislikesCount()-1));
            review.setDislikesCount(review.getDislikesCount()-1);
            holder.disliked = false;
        }else{
            if(holder.liked){
                likeOnClickListener(holder, review);
            }
            database.collection("reviews")
                    .document(review.getId())
                    .update("dislikes-users", FieldValue.arrayUnion(
                            sharedPreferences.getString(StaticClass.EMAIL, " ")));
            database.collection("reviews")
                    .document(review.getId())
                    .update("dislikes-count", FieldValue.increment(1));
            holder.dislikesIV.setImageDrawable(context.getDrawable(R.drawable.ic_dislike_special));
            review.setDislikesCount(review.getDislikesCount()+1);
            holder.disliked = true;
        }
        setDislikesCount(holder, review);
    }
    private void setListeners(final ViewHolder holder, final Review review){
        holder.likesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { likeOnClickListener(holder, review);
            }
        });
        holder.dislikesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dislikeOnClickListener(holder, review);
            }
        });
        holder.reviewerLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ProfileActivity.class)
                        .putExtra(StaticClass.PROFILE_ID, review.getReviewerID()));
            }
        });
    }


    @Override
    public int getItemCount() {
        return reviewsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout reviewerLL;
        private ImageView photoIV, likesIV, dislikesIV;
        private TextView nameTV, usernameTV, timeTV, reviewTV, likesCountTV, dislikesCountTV;
        private View itemView;
        private boolean liked, disliked;

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
            likesIV = itemView.findViewById(R.id.likeIV);
            dislikesIV = itemView.findViewById(R.id.dislikeIV);
            likesCountTV = itemView.findViewById(R.id.likeTV);
            dislikesCountTV = itemView.findViewById(R.id.dislikeTV);
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
