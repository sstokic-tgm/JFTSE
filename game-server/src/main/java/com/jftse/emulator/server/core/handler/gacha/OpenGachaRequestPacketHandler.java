package com.jftse.emulator.server.core.handler.gacha;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.lottery.GachaOpenResult;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.lottery.S2COpenGachaAnswerPacket;
import com.jftse.emulator.server.core.service.LotteryServiceV2;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.gacha.CMSGOpenGacha;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketId(CMSGOpenGacha.PACKET_ID)
public class OpenGachaRequestPacketHandler implements PacketHandler<FTConnection, CMSGOpenGacha> {
    private final LotteryServiceV2 lotteryService;

    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

    public OpenGachaRequestPacketHandler() {
        lotteryService = ServiceManager.getInstance().getLotteryServiceV2();
    }

    @Override
    public void handle(FTConnection connection, CMSGOpenGacha openGachaReqPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        if (client.isUsingGachaMachine()) {
            S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(FAILURE, List.of());
            connection.sendTCP(openGachaAnswerPacket);
            return;
        }

        long playerPocketId = openGachaReqPacket.getPlayerPocketId();
        int productIndex = openGachaReqPacket.getProductIndex();

        try {
            GachaOpenResult gachaOpenResult = lotteryService.openGacha(client, playerPocketId, productIndex);
            logGachaResult(client.getPlayer(), productIndex, gachaOpenResult);

            if (gachaOpenResult.isConsumedGachaRemoved()) {
                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(openGachaReqPacket.getPlayerPocketId());
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            } else if (gachaOpenResult.getConsumedGachaPocket() != null) {
                S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(gachaOpenResult.getConsumedGachaPocket());
                connection.sendTCP(inventoryItemCountPacket);
            }

            if (gachaOpenResult.isSuccess()) {
                S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(SUCCESS, List.of(gachaOpenResult.getAwardedItem()));
                connection.sendTCP(openGachaAnswerPacket);

                GameEventBus.call(GameEventType.GACHA_OPENED, client, productIndex, List.of(gachaOpenResult));
            } else {
                log.debug("Gacha open failed for playerId {}: {}", client.getPlayer().getId(), gachaOpenResult.getFailureReason());

                S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(FAILURE, List.of());
                connection.sendTCP(openGachaAnswerPacket);
            }
        } catch (ValidationException e) {
            log.error("Failed to open gacha for playerId {}: {}", client.getPlayer().getId(), e.getMessage());

            S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(FAILURE, List.of());
            connection.sendTCP(openGachaAnswerPacket);
        }
    }

    private void logGachaResult(FTPlayer player, int productIndex, GachaOpenResult result) {
        PlayerPocket awarded = result.getAwardedItem();
        PlayerPocket consumed = result.getConsumedGachaPocket();

        log.debug(
                "GACHA_OPEN playerId={} productIndex={} success={} awardedItemIndex={} awardedCategory={} awardedCount={} duplicateConverted={} tokensAdded={} pityBefore={} pityAfter={} pityGuaranteed={} rareItemHit={} consumedRemoved={} consumedRemaining={} failureReason={}",
                player.getId(),
                productIndex,
                result.isSuccess(),
                awarded != null ? awarded.getItemIndex() : null,
                awarded != null ? awarded.getCategory() : null,
                awarded != null ? awarded.getItemCount() : null,
                result.isDuplicateConverted(),
                result.getGachaTokensAdded(),
                result.getPityBefore(),
                result.getPityAfter(),
                result.isPityGuaranteed(),
                result.isRareItemHit(),
                result.isConsumedGachaRemoved(),
                consumed != null ? consumed.getItemCount() : 0,
                result.getFailureReason()
        );
    }
}
