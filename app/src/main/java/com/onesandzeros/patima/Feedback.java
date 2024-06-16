package com.onesandzeros.patima;

public class Feedback {

    private int feedback_id;
    private int rating;
    private int img_id;
    private String desc;

    public Feedback(int feedback_id, String desc, int rating, int img_id) {
        this.feedback_id = feedback_id;
        this.desc = desc;
        this.rating = rating;
        this.img_id = img_id;

    }

    public int getFeedback_id() {
        return feedback_id;
    }

    public void setFeedback_id(int feedback_id) {
        this.feedback_id = feedback_id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getImg_id() {
        return img_id;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
