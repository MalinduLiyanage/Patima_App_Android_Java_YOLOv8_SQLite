package com.onesandzeros.patima;

public class Image {

    private int img_id;
    private String inputImg;
    private String outputImg;
    private String timestamp;

    public Image(int img_id, String inputImg, String outputImg, String timestamp) {
        this.img_id = img_id;
        this.inputImg = inputImg;
        this.outputImg = outputImg;
        this.timestamp = timestamp;
    }

    public String getInputImg() {
        return inputImg;
    }

    public void setInputImg(String inputImg) {
        this.inputImg = inputImg;
    }

    public String getOutputImg() {
        return outputImg;
    }

    public void setOutputImg(String outputImg) {
        this.outputImg = outputImg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getImg_id() {
        return img_id;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }
}
