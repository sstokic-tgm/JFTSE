package com.jftse.server.core.shared.rabbit.messages;

import com.jftse.server.core.constants.BallHitAction;
import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class MatchBallSyncMessage extends AbstractBaseMessage {
    private Integer gameSessionId;
    private Integer playerId;
    private Integer playerPos;
    private BallHitAction hitAct;
    private Integer powerLevel;
    private Float speed;
    private Float curveControl;
    private Integer shotCode;
    private Integer specialShotId;
    private Instant timestamp = Instant.now();

    @Builder
    public MatchBallSyncMessage(Integer gameSessionId, Integer playerId, Integer playerPos, BallHitAction hitAct,
                                Integer powerLevel, Float speed, Float curveControl, Integer shotCode, Integer specialShotId) {
        this.gameSessionId = gameSessionId;
        this.playerId = playerId;
        this.playerPos = playerPos;
        this.hitAct = hitAct;
        this.powerLevel = powerLevel;
        this.speed = speed;
        this.curveControl = curveControl;
        this.shotCode = shotCode;
        this.specialShotId = specialShotId;
    }

    @Override
    public String getMessageType() {
        return "MATCH_BALL_SYNC";
    }
}
