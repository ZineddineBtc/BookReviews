package com.example.bookreviews.adapter;

import android.content.Context;
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
import com.example.bookreviews.model.Review;
import com.example.bookreviews.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    public ProfileReviewsAdapter(Context context, List<Review> data,
                                 Bitmap profilePhotoBitmap, User user) {
        this.mInflater = LayoutInflater.from(context);
        this.reviewsList = data;
        this.context = context;
        this.profilePhotoBitmap = profilePhotoBitmap;
        this.user = user;
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
        holder.nameTV.setText(user.getName());
        holder.bioTV.setText(user.getBio());
        holder.usernameTV.setText(user.getUsername());
        holder.timeTV.setText(castTime(review.getTime()));
        holder.titleTV.setText(review.getBook());
        holder.reviewTextTV.setText(review.getReviewText());
    }
    private String castTime(long time){
        return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(time));
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView photoIV;
        private TextView nameTV, bioTV, usernameTV, timeTV, titleTV, reviewTextTV;
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
            bioTV = itemView.findViewById(R.id.bioTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            reviewTextTV = itemView.findViewById(R.id.reviewTV);
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
