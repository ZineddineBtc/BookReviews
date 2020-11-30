package com.example.bookreviews.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreviews.R;
import com.example.bookreviews.StaticClass;
import com.example.bookreviews.activity.core.BookReviewsActivity;
import com.example.bookreviews.activity.core.ProfileActivity;
import com.example.bookreviews.model.Book;
import com.example.bookreviews.model.User;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.ViewHolder> {

    private List<Book> booksList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public SearchBookAdapter(Context context, List<Book> data) {
        this.mInflater = LayoutInflater.from(context);
        this.booksList = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.book_search_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Book book = booksList.get(position);
        holder.titleTV.setText(book.getTitle());
        StringBuilder builder = new StringBuilder(book.getReviewsNumber() + " review");
        if(book.getReviewsNumber()>1) builder.append("s");
        holder.reviewsNumberTV.setText(builder.toString());
        holder.bookLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, BookReviewsActivity.class)
                        .putExtra(StaticClass.BOOK_ID, book.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout bookLL;
        private TextView titleTV, reviewsNumberTV;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        private void findViewsByIds(){
            bookLL = itemView.findViewById(R.id.bookLL);
            titleTV = itemView.findViewById(R.id.titleTV);
            reviewsNumberTV = itemView.findViewById(R.id.reviewsNumberTV);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    Book getItem(int id) {
        return booksList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
