package com.hku.nook.domain;

import java.util.List;

public class AiChatResponse {
    private String content;
    private List<AiChatSource> sources;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<AiChatSource> getSources() {
        return sources;
    }

    public void setSources(List<AiChatSource> sources) {
        this.sources = sources;
    }
}
