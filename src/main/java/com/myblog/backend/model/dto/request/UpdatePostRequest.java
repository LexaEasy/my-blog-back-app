package com.myblog.backend.model.dto.request;

import java.util.List;

public class UpdatePostRequest {
    private String title;
    private String text;
    private Integer likes;
    private Integer comments;
    private List<String> tags;

    public UpdatePostRequest() {
    }

    public UpdatePostRequest(String title, String text, Integer likes, Integer comments, List<String> tags) {
        this.title = title;
        this.text = text;
        this.likes = likes;
        this.comments = comments;
        this.tags = tags;
    }

    public String getTitle() { return title; }
    public String getText() { return text; }
    public Integer getLikes() { return likes; }
    public Integer getComments() { return comments; }
    public List<String> getTags() { return tags; }

    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setViews(Integer views) { this.likes = views; }
    public void setComments(Integer comments) { this.comments = comments; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
