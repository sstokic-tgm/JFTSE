package com.jftse.server.core.shared.rabbit.messages;

import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ServerNoticeMessage extends AbstractBaseMessage {
    private Long accountId;
    private String message;

    @Builder
    public ServerNoticeMessage(Long accountId, String message) {
        this.accountId = accountId;
        this.message = message;
    }

    @Override
    public String getMessageType() {
        return "SERVER_NOTICE";
    }
}
