package com.example.bookreviews.model;

import java.util.ArrayList;

public class Review {
    private String id, book, reviewText,reviewerID, reviewerUsername, reviewerName;
    private long time, likesCount, dislikesCount;
    private boolean liked, disliked;
    private ArrayList<String> likesUsers, dislikesUsers;

    public ArrayList<String> getLikesUsers() {
        return likesUsers;
    }

    public void setLikesUsers(ArrayList<String> likesUsers) {
        this.likesUsers = likesUsers;
    }

    public ArrayList<String> getDislikesUsers() {
        return dislikesUsers;
    }

    public void setDislikesUsers(ArrayList<String> dislikesUsers) {
        this.dislikesUsers = dislikesUsers;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isDisliked() {
        return disliked;
    }

    public void setDisliked(boolean disliked) {
        this.disliked = disliked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(String reviewerID) {
        this.reviewerID = reviewerID;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(long dislikesCount) {
        this.dislikesCount = dislikesCount;
    }
}
