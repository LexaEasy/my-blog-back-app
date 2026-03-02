package com.myblog.backend.model.dto.request;

public class UpdatePostRequest {
    private String title;
    private String text;
    private Integer likes;
    private Integer comments;

    public UpdatePostRequest() {
    }

    public UpdatePostRequest(String title, String text, Integer likes, Integer comments) {
        this.title = title;
        this.text = text;
        this.likes = likes;
        this.comments = comments;
    }

    public String getTitle() { return title; }
    
    public String getText() { return text; }
    public Integer getLikes() { return likes; }
    public Integer getComments() { return comments; }

    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setViews(Integer views) { this.likes = views; }
    public void setComments(Integer comments) { this.comments = comments; }
}