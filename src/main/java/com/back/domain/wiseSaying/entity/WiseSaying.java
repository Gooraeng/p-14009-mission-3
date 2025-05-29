package com.back.domain.wiseSaying.entity;


import java.time.LocalDateTime;

public class WiseSaying {
    private int id;
    private String author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public WiseSaying(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public boolean isNew() {
        return id == 0;
    }

    @Override
    public String toString() {
        return id + " / " + author + " / " + content;
    }
}
