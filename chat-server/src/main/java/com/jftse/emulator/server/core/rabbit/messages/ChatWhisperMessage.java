package com.jftse.emulator.server.core.rabbit.messages;

import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatWhisperMessage extends AbstractBaseMessage {
    private Long senderId;
    private Long receiverId;
    private String message;

    @Builder
    public ChatWhisperMessage(Long senderId, Long receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    @Override
    public String getMessageType() {
        return MessageTypes.CHAT_WHISPER.getValue();
    }
}
