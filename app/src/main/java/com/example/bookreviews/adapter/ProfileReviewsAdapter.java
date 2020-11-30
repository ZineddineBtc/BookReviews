package com.example.bookreviews.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
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

public class ProfileReviewsAdapter extends RecyclerView.Adapter<ProfileReviewsAdapter.ViewHolder> {

    private List<Review> reviewsList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private Bitmap profilePhotoBitmap;
    private User user;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore database;

    public ProfileReviewsAdapter(Context context, List<Review> data,
                                 Bitmap profilePhotoBitmap, User user) {
        this.mInflater = LayoutInflater.from(context);
        this.reviewsList = data;
        this.context = context;
        this.profilePhotoBitmap = profilePhotoBitmap;
        this.user = user;
        this.database = FirebaseFirestore.getInstance();
        this.sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.profile_review_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Review review = reviewsList.get(position);
        holder.photoIV.setImageBitmap(profilePhotoBitmap);
        setTVs(holder, review);
        setLikesCount(holder, review);
        setDislikesCount(holder, review);
        setLikedOrDisliked(holder, review);
        setListeners(holder, review);
    }
    private void setTVs(ViewHolder holder, Review review){
        holder.nameTV.setText(user.getName());
        holder.usernameTV.setText(user.getUsername());
        holder.timeTV.setText(castTime(review.getTime()));
        holder.titleTV.setText(review.getBook());
        holder.reviewTextTV.setText(review.getReviewText());
    }
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
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView photoIV, likesIV, dislikesIV;
        private TextView nameTV, usernameTV, timeTV, titleTV, reviewTextTV,
                            likesCountTV, dislikesCountTV;
        private boolean liked, disliked;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        private void findViewsByIds(){
            photoIV = itemView.findViewById(R.id.photoIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            titleTV = itemView.findViewById(R.id.bookTitleTV);
            reviewTextTV = itemView.findViewById(R.id.reviewTV);
            likesIV = itemView.findViewById(R.id.likesIV);
            dislikesIV = itemView.findViewById(R.id.dislikesIV);
            likesCountTV = itemView.findViewById(R.id.likesCountTV);
            dislikesCountTV = itemView.findViewById(R.id.dislikesCountTV);
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
