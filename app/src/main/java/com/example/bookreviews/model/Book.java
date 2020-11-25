package com.example.bookreviews.model;

public class Book {
    private String id, title;
    private long reviewsNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getReviewsNumber() {
        return reviewsNumber;
    }

    public void setReviewsNumber(long reviewsNumber) {
        this.reviewsNumber = reviewsNumber;
    }
}
