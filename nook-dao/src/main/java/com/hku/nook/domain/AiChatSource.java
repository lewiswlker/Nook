package com.hku.nook.domain;

public class AiChatSource {
    private String type;
    private String title;
    private String url;

    public AiChatSource() {
    }

    public AiChatSource(String type, String title, String url) {
        this.type = type;
        this.title = title;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
