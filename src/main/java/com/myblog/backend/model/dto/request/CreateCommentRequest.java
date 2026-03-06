package com.myblog.backend.model.dto.request;

public class CreateCommentRequest {
    private String text;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
