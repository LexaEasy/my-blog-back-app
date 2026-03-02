package com.myblog.backend.model.dto.request;

public class CreatePostRequest {
    private String title;
    private String text;

    public CreatePostRequest() {
    }

    public CreatePostRequest(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}