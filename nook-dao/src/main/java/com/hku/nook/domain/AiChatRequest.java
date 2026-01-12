package com.hku.nook.domain;

import java.util.List;

public class AiChatRequest {
    private List<AiChatMessage> messages;

    public List<AiChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AiChatMessage> messages) {
        this.messages = messages;
    }
}
