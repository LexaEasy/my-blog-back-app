package com.myblog.backend.model.dto.request;

import java.util.List;

public class CreatePostRequest {
    private String title;
    private String text;
    private List<String> tags;

    public CreatePostRequest() {
    }

    public CreatePostRequest(String title, String text, List<String> tags) {
        this.title = title;
        this.text = text;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
