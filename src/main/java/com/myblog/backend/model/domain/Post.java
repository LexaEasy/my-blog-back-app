package com.myblog.backend.model.domain;

import java.util.List;

public class Post {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private Integer likesCount;
    private Integer commentsCount;

    public Post(Long id, String title, String text, List<String> tags, Integer likesCount, Integer commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.tags = tags;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    public Long getId() {
        return id;
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

    public Integer getLikesCount() {
        return likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }


}
