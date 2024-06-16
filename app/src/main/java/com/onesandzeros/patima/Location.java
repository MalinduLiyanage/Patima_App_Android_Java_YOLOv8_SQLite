package com.onesandzeros.patima;

public class Location {

    private String tags;
    private int img_id;

    public Location(int img_id, String tags) {
        this.img_id = img_id;
        this.tags = tags;

    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getImg_id() {
        return img_id;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }
}
