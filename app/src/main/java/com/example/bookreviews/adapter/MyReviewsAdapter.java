package com.example.bookreviews.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.model.Review;
import com.example.bookreviews.model.User;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyReviewsAdapter extends RecyclerView.Adapter<MyReviewsAdapter.ViewHolder> {

    private List<Review> reviewsList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public MyReviewsAdapter(Context context, List<Review> data) {
        this.mInflater = LayoutInflater.from(context);
        this.reviewsList = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.profile_review_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Review review = reviewsList.get(position);
        holder.timeTV.setText(castTime(review.getTime()));
        holder.titleTV.setText(review.getBook());
        holder.reviewTextTV.setText(review.getReviewText());
        holder.likesCountTV.setText(String.valueOf(review.getLikesCount()));
        holder.dislikesCountTV.setText(String.valueOf(review.getDislikesCount()));
    }

    private String castTime(long time){
        return new SimpleDateFormat("dd MMM. yyyy HH:mm").format(new Date(time));
    }
    @Override
    public int getItemCount() {
        return reviewsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView timeTV, titleTV, reviewTextTV,
                            likesCountTV, dislikesCountTV;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        private void findViewsByIds(){
            timeTV = itemView.findViewById(R.id.timeTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            reviewTextTV = itemView.findViewById(R.id.reviewTV);
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
