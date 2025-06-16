package com.jftse.emulator.server.core.rabbit.messages;

import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MatchFinishedMessage extends AbstractBaseMessage {
    private Integer gameSessionId;
    private Long time;
    private String mode;
    private String winner;
    private String map;
    private List<PlayerDto> players;
    private boolean isBoss;
    private boolean isRandom;
    private boolean isHard;

    @Builder
    public MatchFinishedMessage(Integer gameSessionId, Long time, String mode, String winner, String map, List<PlayerDto> players, boolean isBoss, boolean isRandom, boolean isHard) {
        this.gameSessionId = gameSessionId;
        this.time = time;
        this.mode = mode;
        this.winner = winner;
        this.map = map;
        this.players = players;
        this.isBoss = isBoss;
        this.isRandom = isRandom;
        this.isHard = isHard;
    }

    @Override
    public String getMessageType() {
        return MessageTypes.MATCH_FINISHED.getValue();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PlayerDto {
        private String name;
        private String team;
    }
}
