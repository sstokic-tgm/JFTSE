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
public class RefreshFriendRelationMessage extends AbstractBaseMessage {
    private Long playerId;

    @Builder
    public RefreshFriendRelationMessage(Long playerId) {
        this.playerId = playerId;
    }

    @Override
    public String getMessageType() {
        return MessageTypes.REFRESH_FRIEND_RELATION.getValue();
    }
}
